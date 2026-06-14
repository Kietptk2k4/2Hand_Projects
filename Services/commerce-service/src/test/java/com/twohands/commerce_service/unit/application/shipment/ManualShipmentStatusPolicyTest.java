package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.shipment.ManualShipmentStatusPolicy;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualShipmentStatusPolicyTest {

    @Test
    void allowsDocumentedManualTransitions() {
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.PENDING, ShipmentStatus.READY_TO_SHIP)).isTrue();
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.READY_TO_SHIP, ShipmentStatus.SHIPPED)).isTrue();
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.SHIPPED, ShipmentStatus.DELIVERED)).isTrue();
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.SHIPPED, ShipmentStatus.FAILED)).isTrue();
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.PENDING, ShipmentStatus.CANCELLED)).isTrue();
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.READY_TO_SHIP, ShipmentStatus.CANCELLED)).isTrue();
    }

    @Test
    void rejectsBackwardTransition() {
        assertThat(ManualShipmentStatusPolicy.canTransition(
                ShipmentStatus.DELIVERED, ShipmentStatus.SHIPPED)).isFalse();
    }

    @Test
    void mapsDeliveredToOrderItemDeliveredNotCompleted() {
        assertThat(ManualShipmentStatusPolicy.orderItemStatusForShipmentStatus(ShipmentStatus.DELIVERED))
                .contains(OrderItemStatus.DELIVERED);
        assertThat(ManualShipmentStatusPolicy.orderItemStatusForShipmentStatus(ShipmentStatus.PENDING))
                .isEmpty();
    }

    @Test
    void onlyManualCarriersAreEditable() {
        assertThat(ManualShipmentStatusPolicy.isEditableCarrier(ShipmentCarrier.MANUAL)).isTrue();
        assertThat(ManualShipmentStatusPolicy.isEditableCarrier(ShipmentCarrier.SELF_DELIVERY)).isTrue();
        assertThat(ManualShipmentStatusPolicy.isEditableCarrier(ShipmentCarrier.GHN)).isFalse();
    }
}
