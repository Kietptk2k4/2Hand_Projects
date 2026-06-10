package com.twohands.commerce_service.application.order.viewordersforsupport;

public record ViewOrdersForSupportQuery(
        String status,
        String paymentMethod,
        String from,
        String to,
        String sort,
        Integer page,
        Integer size
) {
}
