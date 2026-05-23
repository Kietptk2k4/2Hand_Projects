package com.twohands.admin_service.unit.outbox;

import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxRetryPolicy;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxRetryPolicyTest {

	@Test
	void shouldTreatBrokerErrorsAsRetryable() {
		assertThat(OutboxRetryPolicy.isRetryableLastError("broker unavailable")).isTrue();
	}

	@Test
	void shouldRejectUnsupportedEventTypeError() {
		assertThat(OutboxRetryPolicy.isRetryableLastError("Unsupported outbox event type for publish: FOO"))
				.isFalse();
	}

	@Test
	void shouldRejectSensitivePayloadError() {
		assertThat(OutboxRetryPolicy.isRetryableLastError("Outbox payload contains sensitive field and cannot be published"))
				.isFalse();
	}

	@Test
	void shouldDelayFailedEventUntilBackoffElapsed() {
		OutboxEvent event = new OutboxEvent(
				UUID.randomUUID(),
				"USER_SUSPENDED",
				UUID.randomUUID(),
				"{}",
				OutboxStatus.PROCESSING,
				2,
				Instant.now().minusSeconds(30),
				null,
				"broker down"
		);
		Instant now = Instant.now();

		assertThat(OutboxRetryPolicy.isBackoffElapsed(event, now, 60)).isFalse();

		OutboxEvent oldEnough = new OutboxEvent(
				event.id(),
				event.eventType(),
				event.aggregateId(),
				event.payload(),
				event.status(),
				event.retryCount(),
				Instant.now().minusSeconds(200),
				event.publishedAt(),
				event.lastError()
		);
		assertThat(OutboxRetryPolicy.isBackoffElapsed(oldEnough, now, 60)).isTrue();
	}

	@Test
	void shouldNotDelayStalePendingRecovery() {
		OutboxEvent event = new OutboxEvent(
				UUID.randomUUID(),
				"USER_SUSPENDED",
				UUID.randomUUID(),
				"{}",
				OutboxStatus.PROCESSING,
				0,
				Instant.now().minusSeconds(10),
				null,
				null
		);

		assertThat(OutboxRetryPolicy.isBackoffElapsed(event, Instant.now(), 60)).isTrue();
	}
}
