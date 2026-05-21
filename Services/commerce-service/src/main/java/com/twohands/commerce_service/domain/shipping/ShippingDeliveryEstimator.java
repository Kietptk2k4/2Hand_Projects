package com.twohands.commerce_service.domain.shipping;

import java.time.LocalDate;

public final class ShippingDeliveryEstimator {

    private ShippingDeliveryEstimator() {
    }

    public static LocalDate estimateDeliveryDate(ShipmentType shipmentType, LocalDate fromDate) {
        ShipmentType type = shipmentType == null ? ShipmentType.STANDARD : shipmentType;
        return switch (type) {
            case SAME_DAY -> fromDate;
            case EXPRESS -> fromDate.plusDays(1);
            default -> fromDate.plusDays(3);
        };
    }
}
