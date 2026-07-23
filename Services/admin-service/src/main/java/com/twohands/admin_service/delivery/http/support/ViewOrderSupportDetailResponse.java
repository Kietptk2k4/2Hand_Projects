package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.OrderSupportActiveRefundRequest;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportItem;
import com.twohands.admin_service.domain.support.OrderSupportOrderTimelineEntry;
import com.twohands.admin_service.domain.support.OrderSupportPayment;
import com.twohands.admin_service.domain.support.OrderSupportPaymentTimelineEntry;
import com.twohands.admin_service.domain.support.OrderSupportShipment;
import com.twohands.admin_service.domain.support.OrderSupportShipmentTimelineEntry;
import com.twohands.admin_service.domain.support.OrderSupportShippingAddress;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ViewOrderSupportDetailResponse(
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
		PaymentResponse payment,
		List<ItemResponse> items,
		List<ShipmentResponse> shipments,
		@JsonProperty("order_timeline") List<OrderTimelineEntryResponse> orderTimeline,
		@JsonProperty("active_refund_request") ActiveRefundRequestResponse activeRefundRequest,
		@JsonProperty("cancellation_note") String cancellationNote,
		@JsonProperty("contact_fields_masked") boolean contactFieldsMasked
) {
	public static ViewOrderSupportDetailResponse from(OrderSupportDetail detail, boolean contactFieldsMasked) {
		return new ViewOrderSupportDetailResponse(
				detail.orderId(),
				detail.buyerId(),
				detail.orderStatus(),
				detail.orderPaymentStatus(),
				detail.paymentMethod(),
				detail.totalAmount(),
				detail.finalAmount(),
				detail.createdAt(),
				detail.updatedAt(),
				detail.completedAt(),
				PaymentResponse.from(detail.payment()),
				detail.items().stream().map(ItemResponse::from).toList(),
				detail.shipments().stream().map(ShipmentResponse::from).toList(),
				detail.orderTimeline().stream().map(OrderTimelineEntryResponse::from).toList(),
				ActiveRefundRequestResponse.from(detail.activeRefundRequest()),
				detail.cancellationNote(),
				contactFieldsMasked
		);
	}

	public record ActiveRefundRequestResponse(
			@JsonProperty("refund_request_id") UUID refundRequestId,
			String status,
			@JsonProperty("requested_by") String requestedBy,
			BigDecimal amount,
			String reason,
			@JsonProperty("requested_at") Instant requestedAt
	) {
		static ActiveRefundRequestResponse from(OrderSupportActiveRefundRequest refundRequest) {
			if (refundRequest == null) {
				return null;
			}
			return new ActiveRefundRequestResponse(
					refundRequest.refundRequestId(),
					refundRequest.status(),
					refundRequest.requestedBy(),
					refundRequest.amount(),
					refundRequest.reason(),
					refundRequest.requestedAt()
			);
		}
	}

	public record PaymentResponse(
			@JsonProperty("payment_id") UUID paymentId,
			String status,
			@JsonProperty("payment_method") String paymentMethod,
			BigDecimal amount,
			String currency,
			@JsonProperty("paid_at") Instant paidAt,
			@JsonProperty("expired_at") Instant expiredAt,
			@JsonProperty("checkout_url_expired_at") Instant checkoutUrlExpiredAt,
			List<PaymentTimelineEntryResponse> timeline
	) {
		static PaymentResponse from(OrderSupportPayment payment) {
			if (payment == null) {
				return null;
			}
			return new PaymentResponse(
					payment.paymentId(),
					payment.status(),
					payment.paymentMethod(),
					payment.amount(),
					payment.currency(),
					payment.paidAt(),
					payment.expiredAt(),
					payment.checkoutUrlExpiredAt(),
					payment.timeline().stream().map(PaymentTimelineEntryResponse::from).toList()
			);
		}
	}

	public record PaymentTimelineEntryResponse(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
		static PaymentTimelineEntryResponse from(OrderSupportPaymentTimelineEntry entry) {
			return new PaymentTimelineEntryResponse(entry.oldStatus(), entry.newStatus(), entry.occurredAt());
		}
	}

	public record ItemResponse(
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
		static ItemResponse from(OrderSupportItem item) {
			return new ItemResponse(
					item.orderItemId(),
					item.productId(),
					item.sellerId(),
					item.shipmentId(),
					item.quantity(),
					item.status(),
					item.unitPriceSnapshot(),
					item.finalPrice(),
					item.skuSnapshot(),
					item.productNameSnapshot(),
					item.imageSnapshot(),
					item.attributesSnapshot(),
					item.shopNameSnapshot(),
					item.shippingFeeAllocated(),
					item.completedAt()
			);
		}
	}

	public record ShipmentResponse(
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
			@JsonProperty("shipping_address") ShippingAddressResponse shippingAddress,
			List<ShipmentTimelineEntryResponse> timeline
	) {
		static ShipmentResponse from(OrderSupportShipment shipment) {
			return new ShipmentResponse(
					shipment.shipmentId(),
					shipment.sellerId(),
					shipment.status(),
					shipment.carrier(),
					shipment.trackingNumber(),
					shipment.shippingFee(),
					shipment.shipmentType(),
					shipment.estimatedDeliveryDate(),
					shipment.shippedAt(),
					shipment.deliveredAt(),
					ShippingAddressResponse.from(shipment.shippingAddress()),
					shipment.timeline().stream().map(ShipmentTimelineEntryResponse::from).toList()
			);
		}
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
		static ShippingAddressResponse from(OrderSupportShippingAddress address) {
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

	public record ShipmentTimelineEntryResponse(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("raw_status") String rawStatus,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
		static ShipmentTimelineEntryResponse from(OrderSupportShipmentTimelineEntry entry) {
			return new ShipmentTimelineEntryResponse(
					entry.oldStatus(),
					entry.newStatus(),
					entry.rawStatus(),
					entry.occurredAt()
			);
		}
	}

	public record OrderTimelineEntryResponse(
			@JsonProperty("old_status") String oldStatus,
			@JsonProperty("new_status") String newStatus,
			@JsonProperty("changed_by") String changedBy,
			String note,
			@JsonProperty("occurred_at") Instant occurredAt
	) {
		static OrderTimelineEntryResponse from(OrderSupportOrderTimelineEntry entry) {
			return new OrderTimelineEntryResponse(
					entry.oldStatus(),
					entry.newStatus(),
					entry.changedBy(),
					entry.note(),
					entry.occurredAt()
			);
		}
	}
}
