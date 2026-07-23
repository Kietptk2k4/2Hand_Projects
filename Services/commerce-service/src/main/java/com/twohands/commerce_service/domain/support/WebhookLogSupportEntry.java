package com.twohands.commerce_service.domain.support;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WebhookLogSupportEntry(
        UUID logId,
        String provider,
        String referenceId,
        String eventType,
        String processingStatus,
        Boolean signatureValid,
        String idempotencyKey,
        Map<String, Object> payloadSummary,
        Instant receivedAt,
        UUID paymentId,
        UUID shipmentId,
        UUID orderId
) {
}
