package com.twohands.commerce_service.application.finance.payout.cancelsellerpayoutrequest;

import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CancelSellerPayoutRequestUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final SellerPayoutRepository sellerPayoutRepository;

    public CancelSellerPayoutRequestUseCase(
            SellerShopRepository sellerShopRepository,
            SellerPayoutRepository sellerPayoutRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
    }

    @Transactional
    public SellerPayoutRequest execute(CancelSellerPayoutRequestCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        sellerPayoutRepository.findPayoutRequestForSeller(command.sellerId(), command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));

        boolean cancelled = sellerPayoutRepository.cancelPayoutRequest(
                command.sellerId(),
                command.payoutRequestId(),
                Instant.now()
        );
        if (!cancelled) {
            throw new AppException(ErrorCode.INVALID_PAYOUT_REQUEST_STATE);
        }

        return sellerPayoutRepository.findPayoutRequestForSeller(command.sellerId(), command.payoutRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
    }

    public String successMessage() {
        return "Huy yeu cau rut tien thanh cong.";
    }
}
