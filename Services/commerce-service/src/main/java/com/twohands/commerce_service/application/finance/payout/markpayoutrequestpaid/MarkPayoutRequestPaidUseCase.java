package com.twohands.commerce_service.application.finance.payout.markpayoutrequestpaid;

import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class MarkPayoutRequestPaidUseCase {

    private final SellerPayoutRepository sellerPayoutRepository;
    private final SellerLedgerRepository sellerLedgerRepository;

    public MarkPayoutRequestPaidUseCase(
            SellerPayoutRepository sellerPayoutRepository,
            SellerLedgerRepository sellerLedgerRepository
    ) {
        this.sellerPayoutRepository = sellerPayoutRepository;
        this.sellerLedgerRepository = sellerLedgerRepository;
    }

    @Transactional
    public SellerPayoutRequest execute(MarkPayoutRequestPaidCommand command) {
        SellerPayoutRequest existing = sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
        if (existing.status() != PayoutRequestStatus.APPROVED) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        Instant now = Instant.now();
        boolean paid = sellerPayoutRepository.markPayoutRequestPaid(
                command.payoutRequestId(),
                command.bankTransferRef().trim(),
                now
        );
        if (!paid) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        sellerLedgerRepository.insertDebitForPayout(
                existing.sellerId(),
                existing.id(),
                existing.amount(),
                now
        );

        return sellerPayoutRepository.findPayoutRequestById(command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
    }

    public String successMessage() {
        return "Ghi nhan chuyen khoan rut tien thanh cong.";
    }
}
