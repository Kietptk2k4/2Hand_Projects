package com.twohands.commerce_service.domain.checkout;

public interface CheckoutFromCartRepository {

    CheckoutPrepareOutcome prepareCheckout(CheckoutFromCartRequest request);
}
