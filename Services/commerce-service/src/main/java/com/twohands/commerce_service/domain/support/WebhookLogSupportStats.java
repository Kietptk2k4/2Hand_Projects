package com.twohands.commerce_service.domain.support;

public record WebhookLogSupportStats(
        long total,
        long pending,
        long invalidSignature,
        long processed,
        long payosCount,
        long ghnCount
) {
}
