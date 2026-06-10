package com.twohands.commerce_service.delivery.http.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.payment.viewpaymentsupport.ViewPaymentsForSupportResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentSupportListEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewPaymentsForSupportResponse(
        int page,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        List<PaymentListEntryResponse> payments
) {
    public static ViewPaymentsForSupportResponse from(ViewPaymentsForSupportResult result) {
        return new ViewPaymentsForSupportResponse(
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.payments().stream().map(PaymentListEntryResponse::from).toList()
        );
    }

    public record PaymentListEntryResponse(
            @JsonProperty("payment_id") UUID paymentId,
            @JsonProperty("order_id") UUID orderId,
            @JsonProperty("payment_method") PaymentMethod paymentMethod,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            @JsonProperty("paid_at") Instant paidAt,
            @JsonProperty("created_at") Instant createdAt
    ) {
        static PaymentListEntryResponse from(PaymentSupportListEntry entry) {
            return new PaymentListEntryResponse(
                    entry.paymentId(),
                    entry.orderId(),
                    entry.paymentMethod(),
                    entry.amount(),
                    entry.currency(),
                    entry.status(),
                    entry.paidAt(),
                    entry.createdAt()
            );
        }
    }
}
