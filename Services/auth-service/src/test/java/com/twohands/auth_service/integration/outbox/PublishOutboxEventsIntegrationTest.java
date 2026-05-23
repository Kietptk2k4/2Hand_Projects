package com.twohands.auth_service.integration.outbox;

import com.twohands.auth_service.application.outbox.OutboxEventPublisher;
import com.twohands.auth_service.application.outbox.PublishOutboxEventsUseCase;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.infrastructure.outbox.OutboxPublishScheduler;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        properties = {
                "auth.outbox.publish.enabled=false",
                "auth.outbox.publish.max-retries=3",
                "auth.outbox.publish.batch-size=20",
                "auth.outbox.retry.enabled=false"
        }
)
@ActiveProfiles("test")
class PublishOutboxEventsIntegrationTest {

    @Autowired
    private PublishOutboxEventsUseCase useCase;

    @Autowired
    private OutboxPublishScheduler scheduler;

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
    void publishSuccessShouldMarkPublishedAndSetPublishedAt() {
        UUID eventId = insertOutboxEvent("PENDING", 0);

        int processed = useCase.execute();

        assertEquals(1, processed);
        assertEquals("PUBLISHED", queryStatus(eventId));
        assertNotNull(queryPublishedAt(eventId));
        assertEquals(0, queryRetryCount(eventId));
    }

    @Test
    void publishFailureShouldMarkFailedAndIncrementRetryCount() {
        UUID eventId = insertOutboxEvent("PENDING", 0);
        publisher.failWith("broker unavailable");

        int processed = useCase.execute();

        assertEquals(1, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(1, queryRetryCount(eventId));
        assertEquals("broker unavailable", queryLastError(eventId));
    }

    @Test
    void shouldNotPickFailedEventsForPublishLane() {
        UUID eventId = insertOutboxEvent("FAILED", 1);

        int processed = useCase.execute();

        assertEquals(0, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(1, queryRetryCount(eventId));
    }

    @Test
    void schedulerDisabledShouldNotRunPublishLogic() {
        UUID eventId = insertOutboxEvent("PENDING", 0);

        scheduler.runPublishJob();

        assertEquals("PENDING", queryStatus(eventId));
    }

    @Test
    void publishFailureShouldRedactSecretsInLastError() {
        UUID eventId = insertOutboxEvent("PENDING", 0);
        publisher.failWith("password=super-secret");

        useCase.execute();

        String lastError = queryLastError(eventId);
        assertNotNull(lastError);
        assertTrue(lastError.contains("***"));
        assertTrue(!lastError.contains("super-secret"));
    }

    private UUID insertOutboxEvent(String status, int retryCount) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO outbox_events(
                            id, event_type, source, payload, status, retry_count, created_at, published_at, last_error
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, NULL, NULL)
                        """,
                eventId,
                "USER_UPDATED",
                "auth-service",
                "{\"user_id\":\"550e8400-e29b-41d4-a716-446655440000\",\"email\":\"u@example.com\"}",
                status,
                retryCount,
                java.sql.Timestamp.from(now)
        );
        return eventId;
    }

    private String queryStatus(UUID eventId) {
        return jdbcTemplate.queryForObject("SELECT status FROM outbox_events WHERE id = ?", String.class, eventId);
    }

    private int queryRetryCount(UUID eventId) {
        return jdbcTemplate.queryForObject("SELECT retry_count FROM outbox_events WHERE id = ?", Integer.class, eventId);
    }

    private Instant queryPublishedAt(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT published_at FROM outbox_events WHERE id = ?",
                Instant.class,
                eventId
        );
    }

    private String queryLastError(UUID eventId) {
        return jdbcTemplate.queryForObject("SELECT last_error FROM outbox_events WHERE id = ?", String.class, eventId);
    }

    @TestConfiguration
    static class PublishOutboxTestConfig {
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
