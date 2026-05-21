package com.twohands.commerce_service.application.order.autocompletedeliveredorder;

public record AutoCompleteDeliveredOrdersResult(
        int candidatesFound,
        int itemsCompleted,
        int ordersCompleted,
        int ordersProcessed,
        int failed
) {
}
