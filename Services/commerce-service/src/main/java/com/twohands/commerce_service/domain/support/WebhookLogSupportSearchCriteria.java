package com.twohands.commerce_service.domain.support;

import java.time.Instant;

public record WebhookLogSupportSearchCriteria(
        String provider,
        String referenceId,
        String processingStatus,
        Instant from,
        Instant to
) {
}
