package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.time.Instant;

final class CommerceShipmentStatusOverrideMapper {

	private CommerceShipmentStatusOverrideMapper() {
	}

	static AdminOverrideShipmentStatusResult toDomain(JsonNode data) {
		if (data == null || data.isMissingNode() || data.isNull()) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
		}
		return new AdminOverrideShipmentStatusResult(
				CommerceIntegrationJsonSupport.requireUuid(data, "shipment_id"),
				CommerceIntegrationJsonSupport.requireUuid(data, "order_id"),
				textOrEmpty(data, "carrier"),
				textOrEmpty(data, "previous_status"),
				textOrEmpty(data, "current_status"),
				textOrEmpty(data, "override_source"),
				data.path("raw_status").isNull() ? null : textOrEmpty(data, "raw_status"),
				data.path("order_items_updated").asInt(0),
				parseInstant(data.path("occurred_at").asText(null))
		);
	}

	private static String textOrEmpty(JsonNode data, String field) {
		JsonNode node = data.get(field);
		if (node == null || node.isNull()) {
			return "";
		}
		return node.asText("").trim();
	}

	private static Instant parseInstant(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		return Instant.parse(raw);
	}
}
