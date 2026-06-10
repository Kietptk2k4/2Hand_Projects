package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AdminFinancePayoutResponse {

	private AdminFinancePayoutResponse() {
	}

	public record Item(
			@JsonProperty("id") UUID id,
			@JsonProperty("seller_id") UUID sellerId,
			@JsonProperty("payout_account_id") UUID payoutAccountId,
			@JsonProperty("amount") BigDecimal amount,
			@JsonProperty("status") String status,
			@JsonProperty("admin_note") String adminNote,
			@JsonProperty("bank_transfer_ref") String bankTransferRef,
			@JsonProperty("requested_at") Instant requestedAt,
			@JsonProperty("approved_at") Instant approvedAt,
			@JsonProperty("paid_at") Instant paidAt,
			@JsonProperty("rejected_at") Instant rejectedAt,
			@JsonProperty("cancelled_at") Instant cancelledAt,
			@JsonProperty("bank_name") String bankName,
			@JsonProperty("bank_account_name") String bankAccountName,
			@JsonProperty("bank_account_number") String bankAccountNumber
	) {
		public static Item from(AdminPayoutRequestItem item) {
			return new Item(
					item.id(),
					item.sellerId(),
					item.payoutAccountId(),
					item.amount(),
					item.status(),
					item.adminNote(),
					item.bankTransferRef(),
					item.requestedAt(),
					item.approvedAt(),
					item.paidAt(),
					item.rejectedAt(),
					item.cancelledAt(),
					item.bankName(),
					item.bankAccountName(),
					item.bankAccountNumber()
			);
		}
	}

	public record ListPayload(
			@JsonProperty("items") List<Item> items,
			@JsonProperty("pagination") Pagination pagination
	) {
		public static ListPayload from(AdminPayoutRequestListResult result) {
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
