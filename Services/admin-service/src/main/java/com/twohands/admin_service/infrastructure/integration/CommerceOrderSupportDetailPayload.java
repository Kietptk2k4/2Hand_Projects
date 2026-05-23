package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record CommerceOrderSupportDetailPayload(
		@JsonProperty("order_id") UUID orderId,
		@JsonProperty("buyer_id") UUID buyerId,
		@JsonProperty("order_status") String orderStatus,
		@JsonProperty("order_payment_status") String orderPaymentStatus,
		@JsonProperty("payment_method") String paymentMethod,
		@JsonProperty("total_amount") BigDecimal totalAmount,
		@JsonProperty("final_amount") BigDecimal finalAmount,
		@JsonProperty("created_at") Instant createdAt,
		@JsonProperty("updated_at") Instant updatedAt,
		@JsonProperty("completed_at") Instant completedAt,
		PaymentPayload payment,
		List<ItemPayload> items,
		List<ShipmentPayload> shipments,
		@JsonProperty("order_timeline") List<OrderTimelinePayload> orderTimeline
) {
	record PaymentPayload(
			@JsonProperty("payment_id") UUID paymentId,
			String status,
			@JsonProperty("payment_method") String paymentMethod,
			BigDecimal amount,
			String currency,
			@JsonProperty("paid_at") Instant paidAt,
			@JsonProperty("expired_at") Instant expiredAt,
			@JsonProperty("checkout_url_expired_at") Instant checkoutUrlExpiredAt,
			List<PaymentTimelinePayload> timeline
	) {
	}

	record PaymentTimelinePayload(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
	}

	record ItemPayload(
			@JsonProperty("order_item_id") UUID orderItemId,
			@JsonProperty("product_id") UUID productId,
			@JsonProperty("seller_id") UUID sellerId,
			@JsonProperty("shipment_id") UUID shipmentId,
			int quantity,
			String status,
			@JsonProperty("unit_price_snapshot") BigDecimal unitPriceSnapshot,
			@JsonProperty("final_price") BigDecimal finalPrice,
			@JsonProperty("sku_snapshot") String skuSnapshot,
			@JsonProperty("product_name_snapshot") String productNameSnapshot,
			@JsonProperty("image_snapshot") String imageSnapshot,
			@JsonProperty("attributes_snapshot") String attributesSnapshot,
			@JsonProperty("shop_name_snapshot") String shopNameSnapshot,
			@JsonProperty("shipping_fee_allocated") BigDecimal shippingFeeAllocated,
			@JsonProperty("completed_at") Instant completedAt
	) {
	}

	record ShipmentPayload(
			@JsonProperty("shipment_id") UUID shipmentId,
			@JsonProperty("seller_id") UUID sellerId,
			String status,
			String carrier,
			@JsonProperty("tracking_number") String trackingNumber,
			@JsonProperty("shipping_fee") BigDecimal shippingFee,
			@JsonProperty("shipment_type") String shipmentType,
			@JsonProperty("estimated_delivery_date") LocalDate estimatedDeliveryDate,
			@JsonProperty("shipped_at") Instant shippedAt,
			@JsonProperty("delivered_at") Instant deliveredAt,
			@JsonProperty("shipping_address") ShippingAddressPayload shippingAddress,
			List<ShipmentTimelinePayload> timeline
	) {
	}

	record ShippingAddressPayload(
			@JsonProperty("receiver_name") String receiverName,
			String phone,
			@JsonProperty("province_code") String provinceCode,
			@JsonProperty("district_code") String districtCode,
			@JsonProperty("ward_code") String wardCode,
			@JsonProperty("address_detail") String addressDetail,
			@JsonProperty("full_address") String fullAddress
	) {
	}

	record ShipmentTimelinePayload(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("raw_status") String rawStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
	}

	record OrderTimelinePayload(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("changed_by") String changedBy,
			String note,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
	}
}
