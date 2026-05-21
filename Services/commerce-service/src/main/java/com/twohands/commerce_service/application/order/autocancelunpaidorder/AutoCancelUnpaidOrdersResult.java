package com.twohands.commerce_service.application.order.autocancelunpaidorder;

public record AutoCancelUnpaidOrdersResult(
        int candidatesFound,
        int cancelled,
        int skipped,
        int failed
) {
}
