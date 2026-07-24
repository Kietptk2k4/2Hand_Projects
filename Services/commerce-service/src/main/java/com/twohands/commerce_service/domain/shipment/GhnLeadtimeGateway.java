package com.twohands.commerce_service.domain.shipment;

public interface GhnLeadtimeGateway {

    GhnLeadtimeResult calculateLeadtime(GhnLeadtimeQuery query);
}
