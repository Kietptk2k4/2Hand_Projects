package com.twohands.commerce_service.domain.product;

import java.time.Instant;

public final class ProductPriceWindow {

    private static final Instant OPEN_END = Instant.parse("9999-12-31T23:59:59Z");

    private ProductPriceWindow() {
    }

    public static boolean overlaps(Instant startA, Instant endA, Instant startB, Instant endB) {
        Instant effectiveEndA = endA == null ? OPEN_END : endA;
        Instant effectiveEndB = endB == null ? OPEN_END : endB;
        return startA.isBefore(effectiveEndB) && startB.isBefore(effectiveEndA);
    }
}
