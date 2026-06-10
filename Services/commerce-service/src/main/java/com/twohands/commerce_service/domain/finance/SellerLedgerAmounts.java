package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;

public record SellerLedgerAmounts(
        BigDecimal grossAmount,
        BigDecimal platformFeeAmount,
        BigDecimal netAmount,
        BigDecimal commissionRateSnapshot
) {
}
