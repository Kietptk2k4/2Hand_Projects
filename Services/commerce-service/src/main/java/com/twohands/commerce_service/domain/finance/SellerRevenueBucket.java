package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;

public record SellerRevenueBucket(
        BigDecimal amount,
        long itemCount
) {
    public static SellerRevenueBucket empty() {
        return new SellerRevenueBucket(BigDecimal.ZERO, 0L);
    }
}
