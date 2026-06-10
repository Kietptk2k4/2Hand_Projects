package com.twohands.commerce_service.application.payment.viewpaymentsupport;

public record ViewPaymentsForSupportQuery(
        String status,
        String paymentMethod,
        String orderId,
        String from,
        String to,
        Integer page,
        Integer size
) {
}
