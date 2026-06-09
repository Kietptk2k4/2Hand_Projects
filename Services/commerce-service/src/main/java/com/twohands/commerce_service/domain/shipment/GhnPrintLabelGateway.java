package com.twohands.commerce_service.domain.shipment;

public interface GhnPrintLabelGateway {

    String generatePrintToken(String ghnOrderCode);
}
