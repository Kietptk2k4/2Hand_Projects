package com.twohands.admin_service.delivery.http.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record AdminActionLogEntryResponse(
		@JsonProperty("log_id")
		UUID logId,

		@JsonProperty("admin_id")
		UUID adminId,

		@JsonProperty("action_type")
		String actionType,

		@JsonProperty("target_type")
		String targetType,

		@JsonProperty("target_id")
		String targetId,

		String status,

		@JsonProperty("request_payload")
		JsonNode requestPayload,

		@JsonProperty("response_payload")
		JsonNode responsePayload,

		@JsonProperty("ip_address")
		String ipAddress,

		@JsonProperty("user_agent")
		String userAgent,

		@JsonProperty("created_at")
		Instant createdAt
) {
}
