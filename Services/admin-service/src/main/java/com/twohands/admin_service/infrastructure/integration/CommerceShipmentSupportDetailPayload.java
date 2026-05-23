package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

record CommerceShipmentSupportDetailPayload(
		@JsonProperty("shipment_id") UUID shipmentId,
		@JsonProperty("order_id") UUID orderId,
		@JsonProperty("seller_id") UUID sellerId,
		@JsonProperty("buyer_id") UUID buyerId,
		@JsonProperty("order_status") String orderStatus,
		String carrier,
		@JsonProperty("shipment_type") String shipmentType,
		@JsonProperty("internal_status") String internalStatus,
		@JsonProperty("carrier_status") String carrierStatus,
		@JsonProperty("ghn_order_code") String ghnOrderCode,
		@JsonProperty("tracking_number") String trackingNumber,
		@JsonProperty("shipping_fee") BigDecimal shippingFee,
		@JsonProperty("cod_amount") BigDecimal codAmount,
		@JsonProperty("weight_gram") Integer weightGram,
		@JsonProperty("estimated_delivery_date") LocalDate estimatedDeliveryDate,
		@JsonProperty("shipped_at") Instant shippedAt,
		@JsonProperty("delivered_at") Instant deliveredAt,
		@JsonProperty("created_at") Instant createdAt,
		@JsonProperty("updated_at") Instant updatedAt,
		@JsonProperty("shipping_address") ShippingAddressPayload shippingAddress,
		@JsonProperty("order_items") List<OrderItemPayload> orderItems,
		@JsonProperty("status_history") List<StatusHistoryPayload> statusHistory,
		@JsonProperty("carrier_webhook_events") List<CarrierWebhookPayload> carrierWebhookEvents
) {
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

	record OrderItemPayload(
			@JsonProperty("order_item_id") UUID orderItemId,
			@JsonProperty("product_name_snapshot") String productNameSnapshot,
			int quantity,
			String status
	) {
	}

	record StatusHistoryPayload(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("raw_status") String rawStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
	}

	record CarrierWebhookPayload(
			@JsonProperty("carrier_status") String carrierStatus,
			boolean processed,
			@JsonProperty("received_at") Instant receivedAt
	) {
	}
}
