package com.twohands.auth_service.integration.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.application.outbox.RetryFailedOutboxEventsUseCase;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.infrastructure.outbox.OutboxRetryScheduler;
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

@SpringBootTest(
        properties = {
                "auth.outbox.retry.enabled=false",
                "auth.outbox.retry.max-retries=3",
                "auth.outbox.retry.pending-timeout-seconds=300",
                "auth.outbox.retry.batch-size=20"
        }
)
@ActiveProfiles("test")
class RetryFailedOutboxEventsIntegrationTest {

    @Autowired
    private RetryFailedOutboxEventsUseCase useCase;

    @Autowired
    private OutboxRetryScheduler scheduler;

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
    void retrySuccessShouldMarkPublishedAndSetPublishedAt() {
        UUID eventId = insertOutboxEvent("FAILED", 0, Instant.now().minusSeconds(60));

        int processed = useCase.execute();

        assertEquals(1, processed);
        assertEquals("PUBLISHED", queryStatus(eventId));
        assertEquals(0, queryRetryCount(eventId));
        assertEquals(1, queryPublishedAtCount(eventId));
    }

    @Test
    void retryFailShouldIncrementRetryAndMarkFailedWithLastError() {
        publisher.failWith("broker unavailable");
        UUID eventId = insertOutboxEvent("FAILED", 1, Instant.now().minusSeconds(60));

        int processed = useCase.execute();

        assertEquals(1, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(2, queryRetryCount(eventId));
        assertEquals("broker unavailable", queryLastError(eventId));
    }

    @Test
    void overMaxRetriesShouldNotBePicked() {
        UUID eventId = insertOutboxEvent("FAILED", 3, Instant.now().minusSeconds(60));

        int processed = useCase.execute();

        assertEquals(0, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(3, queryRetryCount(eventId));
    }

    @Test
    void pendingTimeoutShouldBePickedForRetry() {
        UUID eventId = insertOutboxEvent("PENDING", 0, Instant.now().minusSeconds(1000));

        int processed = useCase.execute();

        assertEquals(1, processed);
        assertEquals("PUBLISHED", queryStatus(eventId));
        assertEquals(1, queryPublishedAtCount(eventId));
    }

    @Test
    void schedulerDisabledShouldNotRunRetryLogic() {
        UUID eventId = insertOutboxEvent("FAILED", 0, Instant.now().minusSeconds(60));

        scheduler.runRetryJob();

        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(0, queryRetryCount(eventId));
    }

    private UUID insertOutboxEvent(String status, int retryCount, Instant createdAt) {
        UUID eventId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO outbox_events(
                            id, event_type, source, payload, status, retry_count, created_at, published_at, last_error
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                eventId,
                "USER_UPDATED",
                "auth-service",
                "{\"user_id\":\"u1\",\"email\":\"u1@example.com\",\"updated_at\":\"2026-05-18T00:00:00Z\"}",
                status,
                retryCount,
                createdAt,
                null,
                null
        );
        return eventId;
    }

    private String queryStatus(UUID eventId) {
        return jdbcTemplate.queryForObject("SELECT status FROM outbox_events WHERE id = ?", String.class, eventId);
    }

    private int queryRetryCount(UUID eventId) {
        return jdbcTemplate.queryForObject("SELECT retry_count FROM outbox_events WHERE id = ?", Integer.class, eventId);
    }

    private int queryPublishedAtCount(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE id = ? AND published_at IS NOT NULL",
                Integer.class,
                eventId
        );
    }

    private String queryLastError(UUID eventId) {
        return jdbcTemplate.queryForObject("SELECT last_error FROM outbox_events WHERE id = ?", String.class, eventId);
    }

    @TestConfiguration
    static class RetryOutboxTestConfig {
        @Bean
        @Primary
        ToggleableOutboxEventPublisher outboxEventPublisher() {
            return new ToggleableOutboxEventPublisher();
        }
    }

    static class ToggleableOutboxEventPublisher implements OutboxEventPublisher {
        private final AtomicBoolean shouldFail = new AtomicBoolean(false);
        private volatile String failMessage = "forced publish failure";

        @Override
        public void publish(OutboxEvent event) {
            if (shouldFail.get()) {
                throw new RuntimeException(failMessage);
            }
        }

        void failWith(String message) {
            this.failMessage = message;
            this.shouldFail.set(true);
        }

        void succeed() {
            this.shouldFail.set(false);
        }
    }
}
