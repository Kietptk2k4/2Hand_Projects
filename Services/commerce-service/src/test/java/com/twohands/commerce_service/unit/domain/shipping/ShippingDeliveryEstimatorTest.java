package com.twohands.commerce_service.unit.domain.shipping;

import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingDeliveryEstimator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ShippingDeliveryEstimatorTest {

    private final LocalDate today = LocalDate.of(2026, 5, 21);

    @Test
    void shouldEstimateStandardDeliveryInThreeDays() {
        assertThat(ShippingDeliveryEstimator.estimateDeliveryDate(ShipmentType.STANDARD, today))
                .isEqualTo(today.plusDays(3));
    }

    @Test
    void shouldEstimateExpressDeliveryInOneDay() {
        assertThat(ShippingDeliveryEstimator.estimateDeliveryDate(ShipmentType.EXPRESS, today))
                .isEqualTo(today.plusDays(1));
    }

    @Test
    void shouldEstimateSameDayDeliveryAsToday() {
        assertThat(ShippingDeliveryEstimator.estimateDeliveryDate(ShipmentType.SAME_DAY, today))
                .isEqualTo(today);
    }
}
