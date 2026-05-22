package com.twohands.commerce_service.domain.cart;

import java.math.BigDecimal;
import java.util.List;

public record ViewCartSummary(
        int activeItemCount,
        int invalidItemCount,
        BigDecimal subtotal,
        boolean canCheckout,
        List<String> warnings
) {
    public static ViewCartSummary empty() {
        return new ViewCartSummary(0, 0, BigDecimal.ZERO, false, List.of());
    }
}
