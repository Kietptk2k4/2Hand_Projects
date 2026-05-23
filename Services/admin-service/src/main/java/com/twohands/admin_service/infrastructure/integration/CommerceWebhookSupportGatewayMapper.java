package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;

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

	private static WebhookSupportLogEntry toEntry(CommerceWebhookLogsSupportPayload.WebhookLogPayload log) {
		return new WebhookSupportLogEntry(
				log.logId(),
				log.provider(),
				log.referenceId(),
				log.eventType(),
				log.processingStatus(),
				log.signatureValid(),
				log.retryCount(),
				log.idempotencyKey(),
				log.payloadSummary(),
				log.receivedAt()
		);
	}
}
