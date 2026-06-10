package com.twohands.commerce_service.application.finance.platform.viewplatformrevenuetrend;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.application.finance.viewsellerrevenuetrend.ViewSellerRevenueTrendUseCase;
import com.twohands.commerce_service.domain.finance.PlatformFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.PlatformRevenueTrendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Service
public class ViewPlatformRevenueTrendUseCase {

    private static final int DEFAULT_TREND_DAYS = 30;

    private final PlatformFinanceReadRepository platformFinanceReadRepository;
    private final Clock clock;

    public ViewPlatformRevenueTrendUseCase(
            PlatformFinanceReadRepository platformFinanceReadRepository,
            Clock clock
    ) {
        this.platformFinanceReadRepository = platformFinanceReadRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PlatformRevenueTrendResult execute(ViewPlatformRevenueTrendCommand command) {
        Instant toExclusive = command.toExclusive()
                .orElseGet(() -> clock.instant().atZone(ZoneOffset.UTC).toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant());
        Instant from = command.from()
                .orElseGet(() -> toExclusive.minus(DEFAULT_TREND_DAYS, ChronoUnit.DAYS));

        FinanceDateRangeResolver.validateTrendRange(from, toExclusive);

        return new PlatformRevenueTrendResult(
                command.granularity(),
                from,
                toExclusive,
                platformFinanceReadRepository.findRevenueTrend(from, toExclusive, command.granularity())
        );
    }

    public String successMessage() {
        return "Lay bieu do doanh thu san thanh cong.";
    }

    public static com.twohands.commerce_service.domain.finance.RevenueTrendGranularity parseGranularity(String value) {
        return ViewSellerRevenueTrendUseCase.parseGranularity(value);
    }
}
