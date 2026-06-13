package com.twohands.commerce_service.application.finance.payout.approvepayoutrequest;

import com.twohands.commerce_service.application.finance.payout.common.PayoutRequestApprovedOutboxService;
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
public class ApprovePayoutRequestUseCase {

    private final SellerPayoutRepository sellerPayoutRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PayoutRequestApprovedOutboxService payoutRequestApprovedOutboxService;

    public ApprovePayoutRequestUseCase(
            SellerPayoutRepository sellerPayoutRepository,
            OutboxEventRepository outboxEventRepository,
            PayoutRequestApprovedOutboxService payoutRequestApprovedOutboxService
    ) {
        this.sellerPayoutRepository = sellerPayoutRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.payoutRequestApprovedOutboxService = payoutRequestApprovedOutboxService;
    }

    @Transactional
    public SellerPayoutRequest execute(ApprovePayoutRequestCommand command) {
        SellerPayoutRequest existing = sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
        if (existing.status() != PayoutRequestStatus.REQUESTED) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        Instant now = Instant.now();
        boolean approved = sellerPayoutRepository.approvePayoutRequest(command.payoutRequestId(), now);
        if (!approved) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        SellerPayoutRequest approvedRequest = sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));

        outboxEventRepository.save(payoutRequestApprovedOutboxService.build(
                approvedRequest.id(),
                approvedRequest.sellerId(),
                approvedRequest.amount(),
                now
        ));

        return approvedRequest;
    }

    public String successMessage() {
        return "Duyet yeu cau rut tien thanh cong.";
    }
}
