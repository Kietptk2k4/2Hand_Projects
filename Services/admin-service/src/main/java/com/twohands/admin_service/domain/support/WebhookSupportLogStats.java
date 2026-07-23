package com.twohands.admin_service.domain.support;

public record WebhookSupportLogStats(
		long total,
		long pending,
		long invalidSignature,
		long processed,
		long payosCount,
		long ghnCount
) {
}
