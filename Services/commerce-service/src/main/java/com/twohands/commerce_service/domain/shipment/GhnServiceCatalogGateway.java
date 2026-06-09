package com.twohands.commerce_service.domain.shipment;

import java.util.List;

public interface GhnServiceCatalogGateway {

    List<GhnServiceOption> listAvailableServices(GhnAvailableServicesQuery query);
}
