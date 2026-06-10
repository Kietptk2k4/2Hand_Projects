package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;

public interface ViewShipmentSupportListRepository {

    long count(ShipmentSupportListSearchCriteria criteria);

    ShipmentSupportListPagedResult search(ShipmentSupportListSearchCriteria criteria, WebhookSupportPageRequest pageRequest);
}
