package com.twohands.commerce_service.application.refund.listadminrefundapprovals;

import com.twohands.commerce_service.domain.order.AdminRefundApprovalListSearchCriteria;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.application.refund.RefundApprovalStatusParser;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public final class AdminRefundApprovalListQueryPolicy {

    private AdminRefundApprovalListQueryPolicy() {
    }

    public static AdminRefundApprovalListSearchCriteria toCriteria(
            String status,
            String q,
            String requestedBy,
            String paymentMethod,
            String from,
            String to
    ) {
        return new AdminRefundApprovalListSearchCriteria(
                RefundApprovalStatusParser.parseOptional(status),
                parseSearchQuery(q),
                parseRequestedBy(requestedBy),
                parsePaymentMethod(paymentMethod),
                parseInstantFrom(from, true),
                parseInstantFrom(to, false)
        );
    }

    private static Optional<String> parseSearchQuery(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        if (trimmed.length() > 100) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Search query is too long");
        }
        return Optional.of(trimmed);
    }

    private static Optional<PaymentRefundRequestedBy> parseRequestedBy(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(PaymentRefundRequestedBy.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid requested_by: " + raw);
        }
    }

    private static Optional<PaymentMethod> parsePaymentMethod(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid payment_method: " + raw);
        }
    }

    private static Optional<Instant> parseInstantFrom(String raw, boolean startOfDay) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        try {
            if (trimmed.length() == 10) {
                LocalDate date = LocalDate.parse(trimmed);
                return Optional.of(
                        startOfDay
                                ? date.atStartOfDay(ZoneOffset.UTC).toInstant()
                                : date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusMillis(1)
                );
            }
            return Optional.of(Instant.parse(trimmed));
        } catch (Exception ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid date: " + raw);
        }
    }

    public static Optional<UUID> parseOrderIdForSearch(String q) {
        if (q == null || q.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(q.trim()));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
