package com.twohands.commerce_service.application.finance.platform.viewplatformtopsellers;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.domain.finance.PlatformFinanceReadRepository;
import com.twohands.commerce_service.domain.finance.PlatformTopSeller;
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
public class ViewPlatformTopSellersUseCase {

    private static final int DEFAULT_SUMMARY_DAYS = 30;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final PlatformFinanceReadRepository platformFinanceReadRepository;
    private final Clock clock;

    public ViewPlatformTopSellersUseCase(
            PlatformFinanceReadRepository platformFinanceReadRepository,
            Clock clock
    ) {
        this.platformFinanceReadRepository = platformFinanceReadRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlatformTopSeller> execute(ViewPlatformTopSellersCommand command) {
        Instant toExclusive = command.toExclusive()
                .orElseGet(() -> clock.instant().atZone(ZoneOffset.UTC).toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant());
        Instant from = command.from()
                .orElseGet(() -> toExclusive.minus(DEFAULT_SUMMARY_DAYS, ChronoUnit.DAYS));

        FinanceDateRangeResolver.validateSummaryRange(Optional.of(from), Optional.of(toExclusive));
        FinanceDateRangeResolver.validateTrendRange(from, toExclusive);

        int limit = command.limit() == null ? DEFAULT_LIMIT : command.limit();
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.INVALID_PAGINATION,
                    "limit must be between 1 and " + MAX_LIMIT,
                    "limit",
                    "invalid"
            );
        }

        return platformFinanceReadRepository.findTopSellers(from, toExclusive, limit);
    }

    public String successMessage() {
        return "Lay top sellers thanh cong.";
    }
}
