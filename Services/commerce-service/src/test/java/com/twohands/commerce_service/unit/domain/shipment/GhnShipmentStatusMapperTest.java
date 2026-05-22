package com.twohands.commerce_service.unit.domain.shipment;

import com.twohands.commerce_service.domain.shipment.GhnShipmentStatusMapper;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhnShipmentStatusMapperTest {

    @Test
    void mapsDocumentedGhnStatuses() {
        assertThat(GhnShipmentStatusMapper.map("picking")).contains(ShipmentStatus.PICKING_UP);
        assertThat(GhnShipmentStatusMapper.map("ready_to_pick")).contains(ShipmentStatus.READY_TO_SHIP);
        assertThat(GhnShipmentStatusMapper.map("delivering")).contains(ShipmentStatus.SHIPPED);
        assertThat(GhnShipmentStatusMapper.map("delivered")).contains(ShipmentStatus.DELIVERED);
        assertThat(GhnShipmentStatusMapper.map("delivery_fail")).contains(ShipmentStatus.FAILED);
        assertThat(GhnShipmentStatusMapper.map("cancel")).contains(ShipmentStatus.CANCELLED);
        assertThat(GhnShipmentStatusMapper.map("returned")).contains(ShipmentStatus.RETURNED);
    }

    @Test
    void returnsEmptyForUnknownStatus() {
        assertThat(GhnShipmentStatusMapper.map("totally_unknown")).isEmpty();
    }
}
