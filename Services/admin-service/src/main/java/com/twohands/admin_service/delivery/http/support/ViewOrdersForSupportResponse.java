package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.OrderSupportListEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewOrdersForSupportResponse(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<OrderListItemResponse> orders
) {
	public static ViewOrdersForSupportResponse from(
			int page,
			int size,
			long totalElements,
			int totalPages,
			List<OrderSupportListEntry> orders
	) {
		return new ViewOrdersForSupportResponse(
				page,
				size,
				totalElements,
				totalPages,
				orders.stream().map(OrderListItemResponse::from).toList()
		);
	}

	public record OrderListItemResponse(
			@JsonProperty("order_id") UUID orderId,
			@JsonProperty("buyer_id") UUID buyerId,
			@JsonProperty("order_status") String orderStatus,
			@JsonProperty("payment_status") String paymentStatus,
			@JsonProperty("payment_method") String paymentMethod,
			@JsonProperty("final_amount") BigDecimal finalAmount,
			@JsonProperty("created_at") Instant createdAt,
			@JsonProperty("updated_at") Instant updatedAt
	) {
		static OrderListItemResponse from(OrderSupportListEntry entry) {
			return new OrderListItemResponse(
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
