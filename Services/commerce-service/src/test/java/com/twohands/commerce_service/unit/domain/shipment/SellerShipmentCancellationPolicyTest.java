package com.twohands.commerce_service.unit.domain.shipment;

import com.twohands.commerce_service.domain.shipment.SellerShipmentCancellationPolicy;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SellerShipmentCancellationPolicyTest {

    @Test
    void allowsGhnCancellationBeforeShipped() {
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.GHN, ShipmentStatus.PENDING))
                .isTrue();
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.GHN, ShipmentStatus.PICKING_UP))
                .isTrue();
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.GHN, ShipmentStatus.READY_TO_SHIP))
                .isTrue();
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.GHN, ShipmentStatus.SHIPPED))
                .isFalse();
    }

    @Test
    void allowsManualCancellationBeforeShipped() {
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.MANUAL, ShipmentStatus.PENDING))
                .isTrue();
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.MANUAL, ShipmentStatus.READY_TO_SHIP))
                .isTrue();
        assertThat(SellerShipmentCancellationPolicy.canCancel(ShipmentCarrier.MANUAL, ShipmentStatus.SHIPPED))
                .isFalse();
    }
}
