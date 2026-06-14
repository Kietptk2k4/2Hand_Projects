package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CreateVnpayCheckoutUrlRepository {

    Optional<PaymentVnpaySnapshot> findPaymentForBuyer(UUID paymentId, UUID buyerId);

    Optional<PaymentVnpaySnapshot> findPaymentByOrderForBuyer(UUID orderId, UUID buyerId);

    CreateVnpayCheckoutUrlResult saveVnpayCheckoutFields(
            UUID paymentId,
            UUID orderId,
            VnpayPaymentUrlResult providerResult,
            Instant occurredAt
    );
}
