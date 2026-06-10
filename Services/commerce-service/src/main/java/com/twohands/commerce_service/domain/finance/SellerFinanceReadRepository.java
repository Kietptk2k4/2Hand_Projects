package com.twohands.commerce_service.domain.finance;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerFinanceReadRepository {

    SellerRevenueSummary findRevenueSummary(UUID sellerId, Optional<Instant> from, Optional<Instant> toExclusive);

    List<SellerRevenueTrendPoint> findRecognizedRevenueTrend(
            UUID sellerId,
            Instant from,
            Instant toExclusive,
            RevenueTrendGranularity granularity
    );
}
