package com.twohands.commerce_service.domain.payment;

import java.time.Instant;

public interface ProcessVnpayPaymentSuccessRepository {

    ProcessPayosPaymentSuccessResult markPaidByVnpayTxnRef(
            String vnpayTxnRef,
            String vnpayTransactionNo,
            String reason,
            String changedBy,
            Instant occurredAt
    );
}
