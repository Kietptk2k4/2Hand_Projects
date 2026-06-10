package com.twohands.commerce_service.unit.domain.shipment;

import com.twohands.commerce_service.domain.shipment.AdminShipmentStatusOverridePolicy;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminShipmentStatusOverridePolicyTest {

    @Test
    void allowsDocumentedGhnTransition() {
        assertThat(AdminShipmentStatusOverridePolicy.canTransition(
                ShipmentCarrier.GHN,
                ShipmentStatus.SHIPPED,
                ShipmentStatus.DELIVERED,
                false
        )).isTrue();
    }

    @Test
    void rejectsInvalidGhnTransition() {
        assertThat(AdminShipmentStatusOverridePolicy.canTransition(
                ShipmentCarrier.GHN,
                ShipmentStatus.PENDING,
                ShipmentStatus.DELIVERED,
                false
        )).isFalse();
    }

    @Test
    void rejectsTerminalTransitionWithoutForce() {
        assertThat(AdminShipmentStatusOverridePolicy.canTransition(
                ShipmentCarrier.GHN,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.RETURNED,
                false
        )).isFalse();
    }

    @Test
    void allowsTerminalTransitionWithForce() {
        assertThat(AdminShipmentStatusOverridePolicy.canTransition(
                ShipmentCarrier.GHN,
                ShipmentStatus.DELIVERED,
                ShipmentStatus.SHIPPED,
                true
        )).isTrue();
    }
}
