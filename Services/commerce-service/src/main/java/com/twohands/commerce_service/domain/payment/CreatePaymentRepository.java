package com.twohands.commerce_service.domain.payment;

public interface CreatePaymentRepository {

    CreatePaymentResult createPayment(CreatePaymentRequest request);
}
