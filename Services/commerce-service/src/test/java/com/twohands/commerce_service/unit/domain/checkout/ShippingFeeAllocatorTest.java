package com.twohands.commerce_service.unit.domain.checkout;

import com.twohands.commerce_service.domain.checkout.ShippingFeeAllocator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingFeeAllocatorTest {

    @Test
    void shouldAllocateProportionallyAndSumToGroupFee() {
        List<BigDecimal> allocations = ShippingFeeAllocator.allocateProportionally(
                BigDecimal.valueOf(50_000),
                List.of(BigDecimal.valueOf(100_000), BigDecimal.valueOf(200_000))
        );

        assertThat(allocations).hasSize(2);
        assertThat(allocations.stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo(BigDecimal.valueOf(50_000));
        assertThat(allocations.get(0)).isEqualByComparingTo(BigDecimal.valueOf(16_667));
        assertThat(allocations.get(1)).isEqualByComparingTo(BigDecimal.valueOf(33_333));
    }

    @Test
    void shouldReturnZerosWhenGroupFeeIsZero() {
        List<BigDecimal> allocations = ShippingFeeAllocator.allocateProportionally(
                BigDecimal.ZERO,
                List.of(BigDecimal.valueOf(10_000), BigDecimal.valueOf(20_000))
        );

        assertThat(allocations).containsExactly(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
