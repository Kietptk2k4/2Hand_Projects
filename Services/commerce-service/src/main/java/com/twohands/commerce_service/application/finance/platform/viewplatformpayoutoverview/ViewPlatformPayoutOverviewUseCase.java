package com.twohands.commerce_service.application.finance.platform.viewplatformpayoutoverview;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.domain.finance.PlatformFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.PlatformPayoutStatusOverview;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ViewPlatformPayoutOverviewUseCase {

    private static final int DEFAULT_SUMMARY_DAYS = 30;

    private final PlatformFinanceReadRepository platformFinanceReadRepository;
    private final Clock clock;

    public ViewPlatformPayoutOverviewUseCase(
            PlatformFinanceReadRepository platformFinanceReadRepository,
            Clock clock
    ) {
        this.platformFinanceReadRepository = platformFinanceReadRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlatformPayoutStatusOverview> execute(ViewPlatformPayoutOverviewCommand command) {
        Instant toExclusive = command.toExclusive()
                .orElseGet(() -> clock.instant().atZone(ZoneOffset.UTC).toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant());
        Instant from = command.from()
                .orElseGet(() -> toExclusive.minus(DEFAULT_SUMMARY_DAYS, ChronoUnit.DAYS));

        FinanceDateRangeResolver.validateSummaryRange(Optional.of(from), Optional.of(toExclusive));
        FinanceDateRangeResolver.validateTrendRange(from, toExclusive);

        return platformFinanceReadRepository.findPayoutOverview(from, toExclusive);
    }

    public String successMessage() {
        return "Lay tong quan payout san thanh cong.";
    }
}
