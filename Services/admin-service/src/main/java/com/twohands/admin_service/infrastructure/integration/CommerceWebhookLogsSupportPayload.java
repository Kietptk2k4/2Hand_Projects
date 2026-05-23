package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

record CommerceWebhookLogsSupportPayload(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<WebhookLogPayload> logs
) {
	record WebhookLogPayload(
			@JsonProperty("log_id") UUID logId,
			String provider,
			@JsonProperty("reference_id") String referenceId,
			@JsonProperty("event_type") String eventType,
			@JsonProperty("processing_status") String processingStatus,
			@JsonProperty("signature_valid") Boolean signatureValid,
			@JsonProperty("retry_count") int retryCount,
			@JsonProperty("idempotency_key") String idempotencyKey,
			@JsonProperty("payload_summary") Map<String, Object> payloadSummary,
			@JsonProperty("received_at") Instant receivedAt
	) {
	}
}
