package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;

public interface ViewOrdersForSupportRepository {

    OrderSupportListPagedResult search(OrderSupportListSearchCriteria criteria, WebhookSupportPageRequest pageRequest);
}
