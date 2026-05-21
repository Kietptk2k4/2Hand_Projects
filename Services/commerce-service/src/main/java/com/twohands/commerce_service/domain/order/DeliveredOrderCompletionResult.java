package com.twohands.commerce_service.domain.order;

public record DeliveredOrderCompletionResult(
        int itemsCompleted,
        boolean orderCompleted,
        boolean paymentMarkedPaid,
        boolean skippedAlreadyCompleted
) {
    public static DeliveredOrderCompletionResult noOp() {
        return new DeliveredOrderCompletionResult(0, false, false, true);
    }
}
