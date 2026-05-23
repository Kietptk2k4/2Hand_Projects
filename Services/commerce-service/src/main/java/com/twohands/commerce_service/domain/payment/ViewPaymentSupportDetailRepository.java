package com.twohands.commerce_service.domain.payment;

import java.util.Optional;
import java.util.UUID;

public interface ViewPaymentSupportDetailRepository {

    Optional<PaymentSupportDetailSnapshot> findByPaymentId(UUID paymentId);
}
