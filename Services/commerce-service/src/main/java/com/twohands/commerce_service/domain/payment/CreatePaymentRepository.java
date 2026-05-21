package com.twohands.commerce_service.domain.payment;

import java.util.UUID;

public interface CreatePaymentRepository {

    boolean existsByOrderId(UUID orderId);

    CreatePaymentResult createPayment(CreatePaymentRequest request);
}
