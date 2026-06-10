package com.twohands.commerce_service.application.finance.payout.createsellerpayoutrequest;

import com.twohands.commerce_service.config.CommerceFinanceProperties;
import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateSellerPayoutRequestUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final SellerPayoutRepository sellerPayoutRepository;
    private final SellerLedgerRepository sellerLedgerRepository;
    private final CommerceFinanceProperties financeProperties;

    public CreateSellerPayoutRequestUseCase(
            SellerShopRepository sellerShopRepository,
            SellerPayoutRepository sellerPayoutRepository,
            SellerLedgerRepository sellerLedgerRepository,
            CommerceFinanceProperties financeProperties
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerPayoutRepository = sellerPayoutRepository;
        this.sellerLedgerRepository = sellerLedgerRepository;
        this.financeProperties = financeProperties;
    }

    @Transactional
    public SellerPayoutRequest execute(CreateSellerPayoutRequestCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        sellerPayoutRepository.findAccountById(command.sellerId(), command.payoutAccountId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_ACCOUNT_NOT_FOUND));

        BigDecimal amount = command.amount();
        BigDecimal minAmount = financeProperties.getMinPayoutAmount();
        if (amount.compareTo(minAmount) < 0) {
            throw new AppException(ErrorCode.PAYOUT_AMOUNT_BELOW_MINIMUM);
        }

        SellerBalanceSummary balance = sellerLedgerRepository.findBalanceSummary(command.sellerId());
        if (amount.compareTo(balance.availableBalance()) > 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_PAYOUT_BALANCE);
        }

        Instant now = Instant.now();
        UUID payoutRequestId = sellerPayoutRepository.createPayoutRequest(
                command.sellerId(),
                command.payoutAccountId(),
                amount,
                now
        );

        return sellerPayoutRepository.findPayoutRequestForSeller(command.sellerId(), payoutRequestId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_REQUEST_NOT_FOUND));
    }

    public String successMessage() {
        return "Tao yeu cau rut tien thanh cong.";
    }
}
