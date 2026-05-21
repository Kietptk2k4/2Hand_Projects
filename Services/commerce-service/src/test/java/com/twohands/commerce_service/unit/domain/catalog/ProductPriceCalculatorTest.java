package com.twohands.commerce_service.unit.domain.catalog;

import com.twohands.commerce_service.domain.catalog.ProductPriceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPriceCalculatorTest {

    @Test
    void shouldUseSalePriceWhenValid() {
        BigDecimal effective = ProductPriceCalculator.effectivePrice(
                new BigDecimal("1000000"),
                new BigDecimal("900000")
        );

        assertThat(effective).isEqualByComparingTo("900000");
    }

    @Test
    void shouldUseBasePriceWhenSalePriceMissing() {
        BigDecimal effective = ProductPriceCalculator.effectivePrice(
                new BigDecimal("1000000"),
                null
        );

        assertThat(effective).isEqualByComparingTo("1000000");
    }

    @Test
    void shouldUseBasePriceWhenSalePriceGreaterThanPrice() {
        BigDecimal effective = ProductPriceCalculator.effectivePrice(
                new BigDecimal("1000000"),
                new BigDecimal("1200000")
        );

        assertThat(effective).isEqualByComparingTo("1000000");
    }
}
