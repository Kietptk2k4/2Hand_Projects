package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface HandlePaymentFailureRepository {

    Optional<LockedPaymentContext> lockPaymentById(UUID paymentId);

    Optional<LockedPaymentContext> lockPaymentByPayosOrderCode(String payosOrderCode);

    Optional<LockedPaymentContext> lockPaymentByVnpayTxnRef(String vnpayTxnRef);

    HandlePaymentFailureResult handleFailure(
            LockedPaymentContext payment,
            PaymentStatus terminalStatus,
            String reason,
            String changedBy,
            String historyPayloadJson,
            Instant occurredAt
    );
}
