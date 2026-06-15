package com.twohands.commerce_service.application.finance.payout.rejectpayoutrequest;

import com.twohands.commerce_service.application.finance.payout.common.PayoutRequestRejectedOutboxService;
import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RejectPayoutRequestUseCase {

    private final SellerPayoutRepository sellerPayoutRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PayoutRequestRejectedOutboxService payoutRequestRejectedOutboxService;

    public RejectPayoutRequestUseCase(
            SellerPayoutRepository sellerPayoutRepository,
            OutboxEventRepository outboxEventRepository,
            PayoutRequestRejectedOutboxService payoutRequestRejectedOutboxService
    ) {
        this.sellerPayoutRepository = sellerPayoutRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.payoutRequestRejectedOutboxService = payoutRequestRejectedOutboxService;
    }

    @Transactional
    public SellerPayoutRequest execute(RejectPayoutRequestCommand command) {
        SellerPayoutRequest existing = sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
        if (existing.status() != PayoutRequestStatus.REQUESTED) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        String resolvedAdminNote = resolveAdminNote(command.adminNote());

        Instant now = Instant.now();
        boolean rejected = sellerPayoutRepository.rejectPayoutRequest(
                command.payoutRequestId(),
                resolvedAdminNote,
                now
        );
        if (!rejected) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        SellerPayoutRequest rejectedRequest = sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));

        outboxEventRepository.save(payoutRequestRejectedOutboxService.build(
                rejectedRequest.id(),
                rejectedRequest.sellerId(),
                rejectedRequest.amount(),
                resolvedAdminNote,
                now
        ));

        return rejectedRequest;
    }

    private String resolveAdminNote(String adminNote) {
        if (adminNote == null || adminNote.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "admin_note is required when rejecting a payout request",
                    "admin_note",
                    "Rejection reason is required."
            );
        }
        return adminNote.trim();
    }

    public String successMessage() {
        return "Tu choi yeu cau rut tien thanh cong.";
    }
}
