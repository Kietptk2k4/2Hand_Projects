package com.twohands.commerce_service.delivery.http.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderItemStatus;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewOrderDetailResponse(
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("order_status") OrderStatus orderStatus,
        @JsonProperty("order_payment_status") PaymentStatus orderPaymentStatus,
        @JsonProperty("payment_method") PaymentMethod paymentMethod,
        @JsonProperty("total_amount") BigDecimal totalAmount,
        @JsonProperty("final_amount") BigDecimal finalAmount,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("completed_at") Instant completedAt,
        PaymentSummaryResponse payment,
        List<OrderItemDetailResponse> items,
        List<ShipmentDetailResponse> shipments,
        @JsonProperty("order_timeline") List<OrderStatusTimelineEntryResponse> orderTimeline,
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
    public record PaymentSummaryResponse(
            @JsonProperty("payment_id") UUID paymentId,
            PaymentStatus status,
            @JsonProperty("payment_method") PaymentMethod paymentMethod,
            BigDecimal amount,
            String currency,
            @JsonProperty("paid_at") Instant paidAt,
            @JsonProperty("expired_at") Instant expiredAt,
            @JsonProperty("checkout_url_expired_at") Instant checkoutUrlExpiredAt,
            List<PaymentStatusTimelineEntryResponse> timeline
    ) {
    }

    public record PaymentStatusTimelineEntryResponse(
            @JsonProperty("old_status") PaymentStatus oldStatus,
            @JsonProperty("new_status") PaymentStatus newStatus,
            @JsonProperty("occurred_at") Instant occurredAt
    ) {
    }

    public record OrderItemDetailResponse(
            @JsonProperty("order_item_id") UUID orderItemId,
            @JsonProperty("product_id") UUID productId,
            @JsonProperty("seller_id") UUID sellerId,
            @JsonProperty("shipment_id") UUID shipmentId,
            int quantity,
            OrderItemStatus status,
            @JsonProperty("unit_price_snapshot") BigDecimal unitPriceSnapshot,
            @JsonProperty("final_price") BigDecimal finalPrice,
            @JsonProperty("sku_snapshot") String skuSnapshot,
            @JsonProperty("product_name_snapshot") String productNameSnapshot,
            @JsonProperty("image_snapshot") String imageSnapshot,
            @JsonProperty("attributes_snapshot") String attributesSnapshot,
            @JsonProperty("shop_name_snapshot") String shopNameSnapshot,
            @JsonProperty("shipping_fee_allocated") BigDecimal shippingFeeAllocated,
            @JsonProperty("completed_at") Instant completedAt,
            @JsonProperty("review_id") UUID reviewId
    ) {
    }

    public record ShipmentDetailResponse(
            @JsonProperty("shipment_id") UUID shipmentId,
            @JsonProperty("seller_id") UUID sellerId,
            ShipmentStatus status,
            String carrier,
            @JsonProperty("tracking_number") String trackingNumber,
            @JsonProperty("shipping_fee") BigDecimal shippingFee,
            @JsonProperty("shipment_type") ShipmentType shipmentType,
            @JsonProperty("estimated_delivery_date") LocalDate estimatedDeliveryDate,
            @JsonProperty("shipped_at") Instant shippedAt,
            @JsonProperty("delivered_at") Instant deliveredAt,
            @JsonProperty("shipping_address") ShippingAddressSnapshotResponse shippingAddress,
            List<ShipmentStatusTimelineEntryResponse> timeline
    ) {
    }

    public record ShippingAddressSnapshotResponse(
            @JsonProperty("receiver_name") String receiverName,
            String phone,
            @JsonProperty("province_code") String provinceCode,
            @JsonProperty("district_code") String districtCode,
            @JsonProperty("ward_code") String wardCode,
            @JsonProperty("address_detail") String addressDetail,
            @JsonProperty("full_address") String fullAddress
    ) {
    }

    public record ShipmentStatusTimelineEntryResponse(
            @JsonProperty("old_status") ShipmentStatus oldStatus,
            @JsonProperty("new_status") ShipmentStatus newStatus,
            @JsonProperty("raw_status") String rawStatus,
            @JsonProperty("occurred_at") Instant occurredAt
    ) {
    }

    public record OrderStatusTimelineEntryResponse(
            @JsonProperty("old_status") OrderStatus oldStatus,
            @JsonProperty("new_status") OrderStatus newStatus,
            @JsonProperty("changed_by") String changedBy,
            String note,
            @JsonProperty("occurred_at") Instant occurredAt
    ) {
    }
}
