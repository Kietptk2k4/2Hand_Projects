package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.time.Instant;

public record PlatformFinanceSummary(
        BigDecimal recognizedGmv,
        long recognizedItemCount,
        BigDecimal totalPlatformFee,
        BigDecimal codPipelineAmount,
        long pendingPayoutCount,
        BigDecimal pendingPayoutAmount,
        BigDecimal paidPayoutAmount,
        Instant from,
        Instant to
) {
}
