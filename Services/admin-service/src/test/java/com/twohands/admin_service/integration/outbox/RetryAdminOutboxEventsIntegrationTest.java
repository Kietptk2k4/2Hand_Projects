package com.twohands.admin_service.integration.outbox;

import com.twohands.admin_service.application.outbox.OutboxEventPublisher;
import com.twohands.admin_service.application.outbox.RetryAdminOutboxEventsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = {
		"admin.outbox.retry.enabled=false",
		"admin.outbox.retry.max-retries=5",
		"admin.outbox.retry.pending-timeout-seconds=60",
		"admin.outbox.retry.batch-size=20",
		"admin.outbox.retry.backoff-seconds-per-attempt=0"
})
@ActiveProfiles("test")
class RetryAdminOutboxEventsIntegrationTest {

	@Autowired
	private RetryAdminOutboxEventsUseCase retryAdminOutboxEventsUseCase;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ToggleableOutboxEventPublisher publisher;

	@BeforeEach
	void cleanTable() {
		publisher.succeed();
		jdbcTemplate.execute("DELETE FROM outbox_events");
	}

	@Test
	void retryFailedEventShouldMarkPublished() {
		UUID eventId = insertOutboxEvent("FAILED", 1, Instant.now().minusSeconds(600), "broker down");

		int processed = retryAdminOutboxEventsUseCase.execute();

		assertEquals(1, processed);
		assertEquals("PUBLISHED", queryStatus(eventId));
		assertNotNull(queryPublishedAt(eventId));
		assertNull(queryLastError(eventId));
	}

	@Test
	void retryStalePendingEventShouldMarkPublished() {
		UUID eventId = insertOutboxEvent("PENDING", 0, Instant.now().minusSeconds(120), null);

		int processed = retryAdminOutboxEventsUseCase.execute();

		assertEquals(1, processed);
		assertEquals("PUBLISHED", queryStatus(eventId));
	}

	@Test
	void retryStaleProcessingEventShouldMarkPublished() {
		UUID eventId = insertOutboxEvent("PROCESSING", 0, Instant.now().minusSeconds(120), null);

		int processed = retryAdminOutboxEventsUseCase.execute();

		assertEquals(1, processed);
		assertEquals("PUBLISHED", queryStatus(eventId));
	}

	@Test
	void retryFailureShouldIncrementRetryCount() {
		UUID eventId = insertOutboxEvent("FAILED", 2, Instant.now().minusSeconds(600), "old error");
		publisher.fail();

		int processed = retryAdminOutboxEventsUseCase.execute();

		assertEquals(1, processed);
		assertEquals("FAILED", queryStatus(eventId));
		assertEquals(3, queryRetryCount(eventId));
		assertNotNull(queryLastError(eventId));
	}

	@Test
	void shouldNotPickRecentPendingEvents() {
		insertOutboxEvent("PENDING", 0, Instant.now(), null);

		int processed = retryAdminOutboxEventsUseCase.execute();

		assertEquals(0, processed);
		assertEquals(1, countByStatus("PENDING"));
	}

	@Test
	void shouldSkipNonRetryableFailedEvent() {
		UUID eventId = insertOutboxEvent(
				"FAILED",
				1,
				Instant.now().minusSeconds(600),
				"Unsupported outbox event type for publish: UNKNOWN_TYPE"
		);

		int processed = retryAdminOutboxEventsUseCase.execute();

		assertEquals(0, processed);
		assertEquals("FAILED", queryStatus(eventId));
		assertEquals(1, queryRetryCount(eventId));
	}

	private UUID insertOutboxEvent(String status, int retryCount, Instant createdAt, String lastError) {
		UUID eventId = UUID.randomUUID();
		UUID aggregateId = UUID.randomUUID();
		jdbcTemplate.update(
				"""
						INSERT INTO outbox_events(
						    id, event_type, aggregate_id, payload, status,
						    retry_count, created_at, published_at, last_error
						)
						VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?)
						""",
				eventId,
				"USER_SUSPENDED",
				aggregateId,
				"{\"user_id\":\"" + aggregateId + "\"}",
				status,
				retryCount,
				java.sql.Timestamp.from(createdAt),
				lastError
		);
		return eventId;
	}

	private String queryStatus(UUID eventId) {
		return jdbcTemplate.queryForObject("SELECT status FROM outbox_events WHERE id = ?", String.class, eventId);
	}

	private int queryRetryCount(UUID eventId) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT retry_count FROM outbox_events WHERE id = ?",
				Integer.class,
				eventId
		);
		return count == null ? 0 : count;
	}

	private Instant queryPublishedAt(UUID eventId) {
		var ts = jdbcTemplate.queryForObject(
				"SELECT published_at FROM outbox_events WHERE id = ?",
				java.sql.Timestamp.class,
				eventId
		);
		return ts == null ? null : ts.toInstant();
	}

	private String queryLastError(UUID eventId) {
		return jdbcTemplate.queryForObject("SELECT last_error FROM outbox_events WHERE id = ?", String.class, eventId);
	}

	private int countByStatus(String status) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE status = ?",
				Integer.class,
				status
		);
		return count == null ? 0 : count;
	}

	@TestConfiguration
	static class OutboxRetryPublisherTestConfig {

		@Bean
		@Primary
		ToggleableOutboxEventPublisher toggleableOutboxEventPublisher() {
			return new ToggleableOutboxEventPublisher();
		}
	}

	static class ToggleableOutboxEventPublisher implements OutboxEventPublisher {

		private final AtomicBoolean shouldSucceed = new AtomicBoolean(true);

		void succeed() {
			shouldSucceed.set(true);
		}

		void fail() {
			shouldSucceed.set(false);
		}

		@Override
		public void publish(com.twohands.admin_service.domain.outbox.OutboxEvent event) {
			if (!shouldSucceed.get()) {
				throw new RuntimeException("broker unavailable");
			}
		}
	}
}
