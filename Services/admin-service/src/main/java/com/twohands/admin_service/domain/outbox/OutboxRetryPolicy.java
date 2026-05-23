package com.twohands.admin_service.domain.outbox;

import java.time.Instant;
import java.util.Locale;

/**
 * Retry eligibility for admin outbox worker (FR_RetryAdminOutboxEvents).
 */
public final class OutboxRetryPolicy {

	private OutboxRetryPolicy() {
	}

	public static boolean isRetryableLastError(String lastError) {
		if (lastError == null || lastError.isBlank()) {
			return true;
		}
		String lower = lastError.toLowerCase(Locale.ROOT);
		return !lower.contains("unsupported outbox event type")
				&& !lower.contains("invalid admin outbox payload")
				&& !lower.contains("cannot serialize admin outbox")
				&& !lower.contains("sensitive field")
				&& !lower.contains("cannot be published");
	}

	/**
	 * Applies exponential-style delay for previously failed events; stale PENDING/PROCESSING
	 * recovery (no last_error, typical stuck worker) is not delayed.
	 */
	public static boolean isBackoffElapsed(OutboxEvent event, Instant now, int backoffSecondsPerAttempt) {
		if (backoffSecondsPerAttempt <= 0) {
			return true;
		}
		boolean previouslyFailed = event.lastError() != null && !event.lastError().isBlank();
		if (!previouslyFailed && event.retryCount() == 0) {
			return true;
		}
		long delaySeconds = (long) Math.max(event.retryCount(), 1) * backoffSecondsPerAttempt;
		return !event.createdAt().plusSeconds(delaySeconds).isAfter(now);
	}

	public static boolean shouldAttemptRetry(OutboxEvent event, Instant now, int backoffSecondsPerAttempt) {
		return isRetryableLastError(event.lastError()) && isBackoffElapsed(event, now, backoffSecondsPerAttempt);
	}
}
