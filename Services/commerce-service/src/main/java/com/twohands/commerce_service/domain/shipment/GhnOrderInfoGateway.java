package com.twohands.commerce_service.domain.shipment;

public interface GhnOrderInfoGateway {

    GhnOrderInfoResult fetchByOrderCode(String orderCode);

    GhnOrderInfoResult fetchByClientOrderCode(String clientOrderCode);
}
