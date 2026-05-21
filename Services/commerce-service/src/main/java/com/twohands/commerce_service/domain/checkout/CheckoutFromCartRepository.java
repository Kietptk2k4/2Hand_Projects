package com.twohands.commerce_service.domain.checkout;

public interface CheckoutFromCartRepository {

    CheckoutFromCartResult checkout(CheckoutFromCartRequest request);
}
