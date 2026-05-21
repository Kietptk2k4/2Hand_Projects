package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CreatePayosCheckoutUrlRepository {

    Optional<PaymentPayosSnapshot> findPaymentForBuyer(UUID paymentId, UUID buyerId);

    CreatePayosCheckoutUrlResult savePayosCheckoutFields(
            UUID paymentId,
            UUID orderId,
            PayosPaymentLinkResult providerResult,
            Instant occurredAt
    );
}
