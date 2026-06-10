package com.twohands.commerce_service.application.finance.payout.rejectpayoutrequest;

import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RejectPayoutRequestUseCase {

    private final SellerPayoutRepository sellerPayoutRepository;

    public RejectPayoutRequestUseCase(SellerPayoutRepository sellerPayoutRepository) {
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    @Transactional
    public SellerPayoutRequest execute(RejectPayoutRequestCommand command) {
        SellerPayoutRequest existing = sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
        if (existing.status() != PayoutRequestStatus.REQUESTED) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        boolean rejected = sellerPayoutRepository.rejectPayoutRequest(
                command.payoutRequestId(),
                command.adminNote(),
                Instant.now()
        );
        if (!rejected) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        return sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
    }

    public String successMessage() {
        return "Tu choi yeu cau rut tien thanh cong.";
    }
}
