package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;

import java.util.List;

final class CommerceWebhookSupportGatewayMapper {

	private CommerceWebhookSupportGatewayMapper() {
	}

	static PagedResult<WebhookSupportLogEntry> toDomain(CommerceWebhookLogsSupportPayload payload) {
		if (payload == null) {
			return new PagedResult<>(List.of(), 1, 20, 0L, 0);
		}
		List<WebhookSupportLogEntry> items = payload.logs() == null
				? List.of()
				: payload.logs().stream().map(CommerceWebhookSupportGatewayMapper::toEntry).toList();
		return new PagedResult<>(
				items,
				payload.page(),
				payload.size(),
				payload.totalElements(),
				payload.totalPages()
		);
	}

	static WebhookSupportLogEntry toEntry(CommerceWebhookLogsSupportPayload.WebhookLogPayload log) {
		return new WebhookSupportLogEntry(
				log.logId(),
				log.provider(),
				log.referenceId(),
				log.eventType(),
				log.processingStatus(),
				log.signatureValid(),
				log.idempotencyKey(),
				log.payloadSummary(),
				log.receivedAt(),
				log.paymentId(),
				log.shipmentId(),
				log.orderId()
		);
	}

	static WebhookSupportLogStats toStats(CommerceWebhookLogsStatsPayload payload) {
		if (payload == null) {
			return new WebhookSupportLogStats(0L, 0L, 0L, 0L, 0L, 0L);
		}
		long payos = payload.byProvider() == null ? 0L : payload.byProvider().payos();
		long ghn = payload.byProvider() == null ? 0L : payload.byProvider().ghn();
		return new WebhookSupportLogStats(
				payload.total(),
				payload.pending(),
				payload.invalidSignature(),
				payload.processed(),
				payos,
				ghn
		);
	}
}
