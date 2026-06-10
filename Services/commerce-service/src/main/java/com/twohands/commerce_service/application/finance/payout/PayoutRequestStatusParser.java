package com.twohands.commerce_service.application.finance.payout;

import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.util.Optional;

public final class PayoutRequestStatusParser {

    private PayoutRequestStatusParser() {
    }

    public static Optional<PayoutRequestStatus> parseOptional(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(PayoutRequestStatus.valueOf(raw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Invalid payout request status: " + raw);
        }
    }
}
