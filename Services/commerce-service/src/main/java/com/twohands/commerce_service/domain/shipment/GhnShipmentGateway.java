package com.twohands.commerce_service.domain.shipment;

public interface GhnShipmentGateway {

    GhnCreateOrderResult createOrder(GhnCreateOrderCommand command);
}
