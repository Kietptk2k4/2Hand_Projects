package com.twohands.commerce_service.unit.domain.product;

import com.twohands.commerce_service.domain.product.ProductPriceWindow;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPriceWindowTest {

    @Test
    void shouldDetectOverlappingWindows() {
        Instant startA = Instant.parse("2026-05-21T10:00:00Z");
        Instant endA = Instant.parse("2026-05-22T10:00:00Z");
        Instant startB = Instant.parse("2026-05-21T12:00:00Z");
        Instant endB = Instant.parse("2026-05-23T10:00:00Z");

        assertThat(ProductPriceWindow.overlaps(startA, endA, startB, endB)).isTrue();
    }

    @Test
    void shouldNotOverlapWhenAdjacent() {
        Instant startA = Instant.parse("2026-05-21T10:00:00Z");
        Instant endA = Instant.parse("2026-05-22T10:00:00Z");
        Instant startB = Instant.parse("2026-05-22T10:00:00Z");
        Instant endB = Instant.parse("2026-05-23T10:00:00Z");

        assertThat(ProductPriceWindow.overlaps(startA, endA, startB, endB)).isFalse();
    }

    @Test
    void shouldTreatOpenEndedWindowsAsOverlapping() {
        Instant startA = Instant.parse("2026-05-21T10:00:00Z");
        Instant startB = Instant.parse("2026-06-01T10:00:00Z");

        assertThat(ProductPriceWindow.overlaps(startA, null, startB, null)).isTrue();
    }
}
