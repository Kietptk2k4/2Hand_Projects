package com.twohands.commerce_service.delivery.http.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewOrderListResponse(
        List<OrderListEntryResponse> orders,
        PageMetaResponse pagination
) {
    public record OrderListEntryResponse(
            @JsonProperty("order_id") UUID orderId,
            @JsonProperty("order_status") OrderStatus orderStatus,
            @JsonProperty("order_payment_status") PaymentStatus orderPaymentStatus,
            @JsonProperty("payment_method") PaymentMethod paymentMethod,
            @JsonProperty("total_amount") BigDecimal totalAmount,
            @JsonProperty("final_amount") BigDecimal finalAmount,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt,
            @JsonProperty("completed_at") Instant completedAt,
            @JsonProperty("item_count") int itemCount,
            @JsonProperty("preview_product_name") String previewProductName,
            @JsonProperty("preview_image_url") String previewImageUrl,
            PaymentSummaryResponse payment,
            @JsonProperty("shipment_summary") ShipmentSummaryResponse shipmentSummary
    ) {
    }

    public record PaymentSummaryResponse(
            @JsonProperty("payment_id") UUID paymentId,
            PaymentStatus status,
            @JsonProperty("payment_method") PaymentMethod paymentMethod,
            BigDecimal amount,
            String currency
    ) {
    }

    public record ShipmentSummaryResponse(
            @JsonProperty("shipment_count") int shipmentCount,
            List<ShipmentStatus> statuses
    ) {
    }
}
