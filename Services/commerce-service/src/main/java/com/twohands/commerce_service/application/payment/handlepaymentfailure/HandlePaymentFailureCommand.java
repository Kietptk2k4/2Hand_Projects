package com.twohands.commerce_service.application.payment.handlepaymentfailure;

import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.util.UUID;

public record HandlePaymentFailureCommand(
        UUID paymentId,
        String payosOrderCode,
        String vnpayTxnRef,
        PaymentStatus terminalStatus,
        String reason,
        String changedBy,
        String historyPayloadJson
) {
    public HandlePaymentFailureCommand {
        if (terminalStatus != PaymentStatus.FAILED
                && terminalStatus != PaymentStatus.CANCELLED
                && terminalStatus != PaymentStatus.EXPIRED) {
            throw new IllegalArgumentException("terminalStatus must be FAILED, CANCELLED, or EXPIRED");
        }
    }

    public static HandlePaymentFailureCommand byPaymentId(
            UUID paymentId,
            PaymentStatus terminalStatus,
            String reason,
            String changedBy,
            String historyPayloadJson
    ) {
        return new HandlePaymentFailureCommand(paymentId, null, null, terminalStatus, reason, changedBy, historyPayloadJson);
    }

    public static HandlePaymentFailureCommand byPayosOrderCode(
            String payosOrderCode,
            PaymentStatus terminalStatus,
            String reason,
            String changedBy,
            String historyPayloadJson
    ) {
        return new HandlePaymentFailureCommand(null, payosOrderCode, null, terminalStatus, reason, changedBy, historyPayloadJson);
    }

    public static HandlePaymentFailureCommand byVnpayTxnRef(
            String vnpayTxnRef,
            PaymentStatus terminalStatus,
            String reason,
            String changedBy,
            String historyPayloadJson
    ) {
        return new HandlePaymentFailureCommand(null, null, vnpayTxnRef, terminalStatus, reason, changedBy, historyPayloadJson);
    }
}
