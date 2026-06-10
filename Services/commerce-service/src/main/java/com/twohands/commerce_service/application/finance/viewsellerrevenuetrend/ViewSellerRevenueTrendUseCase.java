package com.twohands.commerce_service.application.finance.viewsellerrevenuetrend;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;
import com.twohands.commerce_service.domain.finance.SellerFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendPoint;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendResult;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ViewSellerRevenueTrendUseCase {

    private static final int DEFAULT_TREND_DAYS = 30;

    private final SellerShopRepository sellerShopRepository;
    private final SellerFinanceReadRepository sellerFinanceReadRepository;
    private final Clock clock;

    public ViewSellerRevenueTrendUseCase(
            SellerShopRepository sellerShopRepository,
            SellerFinanceReadRepository sellerFinanceReadRepository,
            Clock clock
    ) {
        this.sellerShopRepository = sellerShopRepository;
        this.sellerFinanceReadRepository = sellerFinanceReadRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SellerRevenueTrendResult execute(ViewSellerRevenueTrendCommand command) {
        sellerShopRepository.findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_SHOP_NOT_FOUND));

        Instant toExclusive = command.toExclusive()
                .orElseGet(() -> clock.instant().atZone(ZoneOffset.UTC).toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant());
        Instant from = command.from()
                .orElseGet(() -> toExclusive.minus(DEFAULT_TREND_DAYS, ChronoUnit.DAYS));

        FinanceDateRangeResolver.validateTrendRange(from, toExclusive);

        List<SellerRevenueTrendPoint> points = sellerFinanceReadRepository.findRecognizedRevenueTrend(
                command.sellerId(),
                from,
                toExclusive,
                command.granularity()
        );

        return new SellerRevenueTrendResult(command.granularity(), from, toExclusive, points);
    }

    public String successMessage() {
        return "Lay bieu do doanh thu seller thanh cong.";
    }

    public static RevenueTrendGranularity parseGranularity(String value) {
        if (value == null || value.isBlank()) {
            return RevenueTrendGranularity.DAY;
        }
        try {
            return RevenueTrendGranularity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "granularity",
                    "must be one of DAY, WEEK, MONTH"
            );
        }
    }
}
