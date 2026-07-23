package com.twohands.commerce_service.application.order.viewordersforsupport;

public record ViewOrdersForSupportQuery(
        String status,
        String paymentMethod,
        String paymentStatus,
        String q,
        String from,
        String to,
        String sort,
        Integer page,
        Integer size
) {
}
