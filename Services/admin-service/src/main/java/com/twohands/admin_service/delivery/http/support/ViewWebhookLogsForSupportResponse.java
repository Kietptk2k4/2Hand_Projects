package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ViewWebhookLogsForSupportResponse(
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages,
		List<WebhookLogEntryResponse> logs
) {
	public static ViewWebhookLogsForSupportResponse from(
			int page,
			int size,
			long totalElements,
			int totalPages,
			List<WebhookSupportLogEntry> logs
	) {
		return new ViewWebhookLogsForSupportResponse(
				page,
				size,
				totalElements,
				totalPages,
				logs.stream().map(WebhookLogEntryResponse::from).toList()
		);
	}

	public record WebhookLogEntryResponse(
			@JsonProperty("log_id") UUID logId,
			String provider,
			@JsonProperty("reference_id") String referenceId,
			@JsonProperty("event_type") String eventType,
			@JsonProperty("processing_status") String processingStatus,
			@JsonProperty("signature_valid") Boolean signatureValid,
			@JsonProperty("idempotency_key") String idempotencyKey,
			@JsonProperty("payload_summary") Map<String, Object> payloadSummary,
			@JsonProperty("received_at") Instant receivedAt,
			@JsonProperty("payment_id") UUID paymentId,
			@JsonProperty("shipment_id") UUID shipmentId,
			@JsonProperty("order_id") UUID orderId
	) {
		static WebhookLogEntryResponse from(WebhookSupportLogEntry entry) {
			return new WebhookLogEntryResponse(
					entry.logId(),
					entry.provider(),
					entry.referenceId(),
					entry.eventType(),
					entry.processingStatus(),
					entry.signatureValid(),
					entry.idempotencyKey(),
					entry.payloadSummary(),
					entry.receivedAt(),
					entry.paymentId(),
					entry.shipmentId(),
					entry.orderId()
			);
		}
	}
}
