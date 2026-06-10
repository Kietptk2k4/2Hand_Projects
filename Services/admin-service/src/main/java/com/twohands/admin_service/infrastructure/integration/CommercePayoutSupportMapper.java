package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class CommercePayoutSupportMapper {

	private CommercePayoutSupportMapper() {
	}

	static AdminPayoutRequestListResult toListResult(JsonNode data) {
		JsonNode pagination = data.path("pagination");
		List<AdminPayoutRequestItem> items = new ArrayList<>();
		for (JsonNode item : data.path("items")) {
			items.add(toItem(item));
		}
		return new AdminPayoutRequestListResult(
				items,
				pagination.path("page").asInt(1),
				pagination.path("limit").asInt(20),
				pagination.path("total_items").asLong(0),
				pagination.path("total_pages").asInt(0),
				pagination.path("has_next").asBoolean(false)
		);
	}

	static AdminPayoutRequestItem toItem(JsonNode node) {
		return new AdminPayoutRequestItem(
				parseUuid(node, "id"),
				parseUuid(node, "seller_id"),
				parseUuid(node, "payout_account_id"),
				new BigDecimal(node.path("amount").asText("0")),
				text(node, "status"),
				text(node, "admin_note"),
				text(node, "bank_transfer_ref"),
				parseInstant(node, "requested_at"),
				parseInstant(node, "approved_at"),
				parseInstant(node, "paid_at"),
				parseInstant(node, "rejected_at"),
				parseInstant(node, "cancelled_at"),
				text(node, "bank_name"),
				text(node, "bank_account_name"),
				text(node, "bank_account_number")
		);
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? null : value.asText();
	}

	private static UUID parseUuid(JsonNode node, String field) {
		return CommerceIntegrationJsonSupport.parseUuid(node, field);
	}

	private static Instant parseInstant(JsonNode node, String field) {
		String raw = text(node, field);
		if (raw == null || raw.isBlank()) {
			return null;
		}
		return Instant.parse(raw);
	}
}
