package com.twohands.commerce_service.domain.shipment;

public interface GhnShippingFeeGateway {

    GhnShippingFeeResult calculateFee(GhnShippingFeeQuery query);
}
