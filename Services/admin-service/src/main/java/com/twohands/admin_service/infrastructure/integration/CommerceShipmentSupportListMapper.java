package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class CommerceShipmentSupportListMapper {

	private CommerceShipmentSupportListMapper() {
	}

	static PagedResult<ShipmentSupportListEntry> toDomain(JsonNode data) {
		if (data == null || data.isMissingNode() || data.isNull()) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
		}
		int page = data.path("page").asInt(1);
		int size = data.path("size").asInt(20);
		long totalElements = data.path("total_elements").asLong(0);
		int totalPages = data.path("total_pages").asInt(0);

		List<ShipmentSupportListEntry> items = new ArrayList<>();
		JsonNode shipments = data.path("shipments");
		if (shipments.isArray()) {
			for (JsonNode node : shipments) {
				items.add(toEntry(node));
			}
		}
		return new PagedResult<>(items, page, size, totalElements, totalPages);
	}

	private static ShipmentSupportListEntry toEntry(JsonNode node) {
		return new ShipmentSupportListEntry(
				CommerceIntegrationJsonSupport.requireUuid(node, "shipment_id"),
				CommerceIntegrationJsonSupport.requireUuid(node, "order_id"),
				CommerceIntegrationJsonSupport.requireUuid(node, "seller_id"),
				textOrEmpty(node, "carrier"),
				textOrEmpty(node, "internal_status"),
				textOrNull(node, "tracking_number"),
				textOrNull(node, "ghn_order_code"),
				parseInstant(node.path("shipped_at").asText(null)),
				parseInstant(node.path("created_at").asText(null)),
				parseInstant(node.path("updated_at").asText(null))
		);
	}

	private static String textOrEmpty(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return "";
		}
		return value.asText("").trim();
	}

	private static String textOrNull(JsonNode node, String field) {
		JsonNode value = node.get(field);
		if (value == null || value.isNull()) {
			return null;
		}
		String text = value.asText("").trim();
		return text.isEmpty() ? null : text;
	}

	private static Instant parseInstant(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		return Instant.parse(raw);
	}
}
