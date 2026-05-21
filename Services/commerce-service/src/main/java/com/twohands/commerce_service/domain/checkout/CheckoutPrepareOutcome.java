package com.twohands.commerce_service.domain.checkout;

import java.util.Optional;

public record CheckoutPrepareOutcome(
        Optional<CheckoutFromCartResult> idempotentResult,
        Optional<CheckoutPreparedData> preparedData
) {
    public static CheckoutPrepareOutcome idempotent(CheckoutFromCartResult result) {
        return new CheckoutPrepareOutcome(Optional.of(result), Optional.empty());
    }

    public static CheckoutPrepareOutcome prepared(CheckoutPreparedData data) {
        return new CheckoutPrepareOutcome(Optional.empty(), Optional.of(data));
    }
}
