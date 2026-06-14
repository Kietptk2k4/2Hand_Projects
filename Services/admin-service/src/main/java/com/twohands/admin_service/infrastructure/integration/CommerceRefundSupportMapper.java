package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class CommerceRefundSupportMapper {

	private CommerceRefundSupportMapper() {
	}

	static AdminRefundApprovalListResult toListResult(JsonNode data) {
		JsonNode pagination = data.path("pagination");
		List<AdminRefundApprovalItem> items = new ArrayList<>();
		for (JsonNode item : data.path("items")) {
			items.add(toItem(item));
		}
		return new AdminRefundApprovalListResult(
				items,
				pagination.path("page").asInt(1),
				pagination.path("limit").asInt(20),
				pagination.path("total_items").asLong(0),
				pagination.path("total_pages").asInt(0),
				pagination.path("has_next").asBoolean(false)
		);
	}

	static AdminRefundApprovalItem toItem(JsonNode node) {
		return new AdminRefundApprovalItem(
				parseUuid(node, "id"),
				parseUuid(node, "payment_id"),
				parseUuid(node, "order_id"),
				parseUuid(node, "buyer_id"),
				text(node, "requested_by"),
				parseUuid(node, "requested_by_user_id"),
				text(node, "status"),
				new BigDecimal(node.path("amount").asText("0")),
				text(node, "reason"),
				text(node, "admin_note"),
				text(node, "payment_method"),
				text(node, "order_payment_status"),
				text(node, "order_status"),
				parseInstant(node, "requested_at"),
				parseInstant(node, "confirmed_at"),
				parseInstant(node, "rejected_at")
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
