package com.twohands.commerce_service.domain.shipment;

public enum ShipmentStatus {
    PENDING,
    PICKING_UP,
    READY_TO_SHIP,
    SHIPPED,
    DELIVERED,
    FAILED,
    CANCELLED,
    RETURNED
}
