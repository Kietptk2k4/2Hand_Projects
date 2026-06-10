package com.twohands.commerce_service.domain.finance;

import java.time.Instant;
import java.util.List;

public interface PlatformFinanceReadRepository {

    PlatformFinanceSummary findPlatformSummary(Instant from, Instant toExclusive);

    PlatformCodPipeline findCodPipeline();

    List<PlatformTopSeller> findTopSellers(Instant from, Instant toExclusive, int limit);

    List<PlatformRevenueTrendPoint> findRevenueTrend(
            Instant from,
            Instant toExclusive,
            RevenueTrendGranularity granularity
    );

    List<PlatformPayoutStatusOverview> findPayoutOverview(Instant from, Instant toExclusive);
}
