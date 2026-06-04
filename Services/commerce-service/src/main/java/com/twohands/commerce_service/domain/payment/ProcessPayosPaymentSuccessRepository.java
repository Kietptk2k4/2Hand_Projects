package com.twohands.commerce_service.domain.payment;

import java.time.Instant;

public interface ProcessPayosPaymentSuccessRepository {

    ProcessPayosPaymentSuccessResult markPaidByPayosOrderCode(
            String payosOrderCode,
            String reason,
            String changedBy,
            Instant occurredAt
    );
}
