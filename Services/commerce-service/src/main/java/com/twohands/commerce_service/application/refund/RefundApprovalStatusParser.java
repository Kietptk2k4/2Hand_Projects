package com.twohands.commerce_service.application.refund;

import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.util.Locale;
import java.util.Optional;

public final class RefundApprovalStatusParser {

    private RefundApprovalStatusParser() {
    }

    public static Optional<PaymentRefundRequestStatus> parseOptional(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(PaymentRefundRequestStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid refund request status: " + rawStatus);
        }
    }
}
