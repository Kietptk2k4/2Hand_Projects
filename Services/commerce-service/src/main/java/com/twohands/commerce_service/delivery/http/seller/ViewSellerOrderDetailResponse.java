package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewSellerOrderDetailResponse(
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("order_status") OrderStatus orderStatus,
        @JsonProperty("order_payment_status") PaymentStatus orderPaymentStatus,
        @JsonProperty("order_payment_method") PaymentMethod orderPaymentMethod,
        @JsonProperty("order_created_at") Instant orderCreatedAt,
        SellerOrderListPaymentSummaryResponse payment,
        @JsonProperty("seller_items_subtotal") BigDecimal sellerItemsSubtotal,
        @JsonProperty("seller_shipping_total") BigDecimal sellerShippingTotal,
        List<SellerOrderListEntryResponse> items,
        @JsonProperty("shipping_address") ShippingAddressSnapshotResponse shippingAddress,
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("buyer_display_name") String buyerDisplayName,
        @JsonProperty("buyer_avatar_url") String buyerAvatarUrl,
        @JsonProperty("active_refund_request") ActiveRefundRequestResponse activeRefundRequest
) {
    public record ActiveRefundRequestResponse(
            @JsonProperty("refund_request_id") UUID refundRequestId,
            String status,
            @JsonProperty("requested_by") String requestedBy,
            BigDecimal amount,
            @JsonProperty("requested_at") Instant requestedAt
    ) {
    }
}
