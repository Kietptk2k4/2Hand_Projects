package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.ShipmentSupportCarrierWebhookEvent;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportOrderItem;
import com.twohands.admin_service.domain.support.ShipmentSupportShippingAddress;
import com.twohands.admin_service.domain.support.ShipmentSupportStatusHistoryEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewShipmentSupportDetailResponse(
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
		@JsonProperty("shipping_address") ShippingAddressResponse shippingAddress,
		@JsonProperty("order_items") List<OrderItemResponse> orderItems,
		@JsonProperty("status_history") List<StatusHistoryEntryResponse> statusHistory,
		@JsonProperty("carrier_webhook_events") List<CarrierWebhookEventResponse> carrierWebhookEvents,
		@JsonProperty("contact_fields_masked") boolean contactFieldsMasked
) {
	public static ViewShipmentSupportDetailResponse from(ShipmentSupportDetail detail, boolean contactFieldsMasked) {
		return new ViewShipmentSupportDetailResponse(
				detail.shipmentId(),
				detail.orderId(),
				detail.sellerId(),
				detail.buyerId(),
				detail.orderStatus(),
				detail.carrier(),
				detail.shipmentType(),
				detail.internalStatus(),
				detail.carrierStatus(),
				detail.ghnOrderCode(),
				detail.trackingNumber(),
				detail.shippingFee(),
				detail.codAmount(),
				detail.weightGram(),
				detail.estimatedDeliveryDate(),
				detail.shippedAt(),
				detail.deliveredAt(),
				detail.createdAt(),
				detail.updatedAt(),
				ShippingAddressResponse.from(detail.shippingAddress()),
				detail.orderItems().stream().map(OrderItemResponse::from).toList(),
				detail.statusHistory().stream().map(StatusHistoryEntryResponse::from).toList(),
				detail.carrierWebhookEvents().stream().map(CarrierWebhookEventResponse::from).toList(),
				contactFieldsMasked
		);
	}

	public record ShippingAddressResponse(
			@JsonProperty("receiver_name") String receiverName,
			String phone,
			@JsonProperty("province_code") String provinceCode,
			@JsonProperty("district_code") String districtCode,
			@JsonProperty("ward_code") String wardCode,
			@JsonProperty("address_detail") String addressDetail,
			@JsonProperty("full_address") String fullAddress
	) {
		static ShippingAddressResponse from(ShipmentSupportShippingAddress address) {
			if (address == null) {
				return null;
			}
			return new ShippingAddressResponse(
					address.receiverName(),
					address.phone(),
					address.provinceCode(),
					address.districtCode(),
					address.wardCode(),
					address.addressDetail(),
					address.fullAddress()
			);
		}
	}

	public record OrderItemResponse(
			@JsonProperty("order_item_id") UUID orderItemId,
			@JsonProperty("product_name_snapshot") String productNameSnapshot,
			int quantity,
			String status
	) {
		static OrderItemResponse from(ShipmentSupportOrderItem item) {
			return new OrderItemResponse(
					item.orderItemId(),
					item.productNameSnapshot(),
					item.quantity(),
					item.status()
			);
		}
	}

	public record StatusHistoryEntryResponse(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("raw_status") String rawStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
		static StatusHistoryEntryResponse from(ShipmentSupportStatusHistoryEntry entry) {
			return new StatusHistoryEntryResponse(
					entry.oldStatus(),
					entry.newStatus(),
					entry.rawStatus(),
					entry.occurredAt()
			);
		}
	}

	public record CarrierWebhookEventResponse(
			@JsonProperty("carrier_status") String carrierStatus,
			boolean processed,
			@JsonProperty("received_at") Instant receivedAt
	) {
		static CarrierWebhookEventResponse from(ShipmentSupportCarrierWebhookEvent event) {
			return new CarrierWebhookEventResponse(
					event.carrierStatus(),
					event.processed(),
					event.receivedAt()
			);
		}
	}
}
