package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AdminFinanceRefundResponse {

	private AdminFinanceRefundResponse() {
	}

	public record Item(
			@JsonProperty("id") UUID id,
			@JsonProperty("payment_id") UUID paymentId,
			@JsonProperty("order_id") UUID orderId,
			@JsonProperty("buyer_id") UUID buyerId,
			@JsonProperty("requested_by") String requestedBy,
			@JsonProperty("requested_by_user_id") UUID requestedByUserId,
			@JsonProperty("status") String status,
			@JsonProperty("amount") BigDecimal amount,
			@JsonProperty("reason") String reason,
			@JsonProperty("admin_note") String adminNote,
			@JsonProperty("payment_method") String paymentMethod,
			@JsonProperty("order_payment_status") String orderPaymentStatus,
			@JsonProperty("order_status") String orderStatus,
			@JsonProperty("requested_at") Instant requestedAt,
			@JsonProperty("confirmed_at") Instant confirmedAt,
			@JsonProperty("rejected_at") Instant rejectedAt
	) {
		public static Item from(AdminRefundApprovalItem item) {
			return new Item(
					item.id(),
					item.paymentId(),
					item.orderId(),
					item.buyerId(),
					item.requestedBy(),
					item.requestedByUserId(),
					item.status(),
					item.amount(),
					item.reason(),
					item.adminNote(),
					item.paymentMethod(),
					item.orderPaymentStatus(),
					item.orderStatus(),
					item.requestedAt(),
					item.confirmedAt(),
					item.rejectedAt()
			);
		}
	}

	public record ListPayload(
			@JsonProperty("items") List<Item> items,
			@JsonProperty("pagination") Pagination pagination
	) {
		public static ListPayload from(AdminRefundApprovalListResult result) {
			return new ListPayload(
					result.items().stream().map(Item::from).toList(),
					new Pagination(
							result.page(),
							result.limit(),
							result.totalItems(),
							result.totalPages(),
							result.hasNext()
					)
			);
		}
	}

	public record Pagination(
			@JsonProperty("page") int page,
			@JsonProperty("limit") int limit,
			@JsonProperty("total_items") long totalItems,
			@JsonProperty("total_pages") int totalPages,
			@JsonProperty("has_next") boolean hasNext
	) {
	}
}
