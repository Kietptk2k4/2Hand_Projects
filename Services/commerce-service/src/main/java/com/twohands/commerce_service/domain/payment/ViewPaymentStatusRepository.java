package com.twohands.commerce_service.domain.payment;

import java.util.Optional;
import java.util.UUID;

public interface ViewPaymentStatusRepository {

    Optional<ViewPaymentStatusSnapshot> findByPaymentIdAndBuyerId(UUID paymentId, UUID buyerId);
}
