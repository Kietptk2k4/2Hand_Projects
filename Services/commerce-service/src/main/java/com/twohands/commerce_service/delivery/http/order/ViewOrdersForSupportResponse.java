package com.twohands.commerce_service.delivery.http.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.order.viewordersforsupport.ViewOrdersForSupportResult;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.OrderSupportListEntry;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewOrdersForSupportResponse(
        int page,
        int size,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        List<OrderListEntryResponse> orders
) {
    public static ViewOrdersForSupportResponse from(ViewOrdersForSupportResult result) {
        return new ViewOrdersForSupportResponse(
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.orders().stream().map(OrderListEntryResponse::from).toList()
        );
    }

    public record OrderListEntryResponse(
            @JsonProperty("order_id") UUID orderId,
            @JsonProperty("buyer_id") UUID buyerId,
            @JsonProperty("order_status") OrderStatus orderStatus,
            @JsonProperty("payment_status") PaymentStatus paymentStatus,
            @JsonProperty("payment_method") PaymentMethod paymentMethod,
            @JsonProperty("final_amount") BigDecimal finalAmount,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt
    ) {
        static OrderListEntryResponse from(OrderSupportListEntry entry) {
            return new OrderListEntryResponse(
                    entry.orderId(),
                    entry.buyerId(),
                    entry.orderStatus(),
                    entry.paymentStatus(),
                    entry.paymentMethod(),
                    entry.finalAmount(),
                    entry.createdAt(),
                    entry.updatedAt()
            );
        }
    }
}
