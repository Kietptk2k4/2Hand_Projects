package com.twohands.commerce_service.application.finance.admin.viewadminsellerfinance;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;
import com.twohands.commerce_service.domain.finance.SellerFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import com.twohands.commerce_service.domain.finance.SellerRevenueSummary;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewAdminSellerFinanceSummaryUseCase {

    private final SellerShopRepository sellerShopRepository;
    private final SellerFinanceReadRepository sellerFinanceReadRepository;
    private final SellerLedgerRepository sellerLedgerRepository;

    public ViewAdminSellerFinanceSummaryUseCase(
            SellerShopRepository sellerShopRepository,
            SellerFinanceReadRepository sellerFinanceReadRepository,
            SellerLedgerRepository sellerLedgerRepository
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerFinanceReadRepository = sellerFinanceReadRepository;
        this.sellerLedgerRepository = sellerLedgerRepository;
    }

    @Transactional(readOnly = true)
    public SellerRevenueSummary execute(ViewAdminSellerFinanceSummaryCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        FinanceDateRangeResolver.validateSummaryRange(command.from(), command.toExclusive());
        SellerRevenueSummary summary = sellerFinanceReadRepository.findRevenueSummary(
                command.sellerId(),
                command.from(),
                command.toExclusive()
        );
        SellerBalanceSummary balance = sellerLedgerRepository.findBalanceSummary(command.sellerId());
        return new SellerRevenueSummary(
                summary.inTransit(),
                summary.pendingConfirm(),
                summary.recognized(),
                summary.totalGross(),
                balance,
                summary.from(),
                summary.to()
        );
    }

    public String successMessage() {
        return "Lay tong hop tai chinh seller thanh cong.";
    }
}
