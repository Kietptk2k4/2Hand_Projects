package com.twohands.commerce_service.application.finance.platform.viewplatformsummary;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.domain.finance.PlatformFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.PlatformFinanceSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class ViewPlatformFinanceSummaryUseCase {

    private static final int DEFAULT_SUMMARY_DAYS = 30;

    private final PlatformFinanceReadRepository platformFinanceReadRepository;
    private final Clock clock;

    public ViewPlatformFinanceSummaryUseCase(
            PlatformFinanceReadRepository platformFinanceReadRepository,
            Clock clock
    ) {
        this.platformFinanceReadRepository = platformFinanceReadRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PlatformFinanceSummary execute(ViewPlatformFinanceSummaryCommand command) {
        Instant toExclusive = command.toExclusive()
                .orElseGet(() -> clock.instant().atZone(ZoneOffset.UTC).toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant());
        Instant from = command.from()
                .orElseGet(() -> toExclusive.minus(DEFAULT_SUMMARY_DAYS, ChronoUnit.DAYS));

        FinanceDateRangeResolver.validateSummaryRange(Optional.of(from), Optional.of(toExclusive));
        FinanceDateRangeResolver.validateTrendRange(from, toExclusive);

        return platformFinanceReadRepository.findPlatformSummary(from, toExclusive);
    }

    public String successMessage() {
        return "Lay tong hop tai chinh san thanh cong.";
    }
}
