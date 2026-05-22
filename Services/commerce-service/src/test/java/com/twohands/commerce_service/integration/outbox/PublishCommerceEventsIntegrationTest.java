package com.twohands.commerce_service.integration.outbox;

import com.twohands.commerce_service.application.outbox.OutboxEventPublisher;
import com.twohands.commerce_service.application.outbox.PublishCommerceEventsUseCase;
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

@SpringBootTest(
        properties = {
                "commerce.outbox.publish.enabled=false",
                "commerce.outbox.publish.max-retries=3",
                "commerce.outbox.publish.batch-size=20"
        }
)
@ActiveProfiles("test")
class PublishCommerceEventsIntegrationTest {

    @Autowired
    private PublishCommerceEventsUseCase useCase;

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
        publisher.fail();

        int processed = useCase.execute();

        assertEquals(1, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(1, queryRetryCount(eventId));
        assertNotNull(queryLastError(eventId));
    }

    private UUID insertOutboxEvent(String status, int retryCount) {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO outbox_events(
                            id, event_type, event_key, aggregate_id, source, payload, status,
                            retry_count, created_at, published_at, last_error
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL)
                        """,
                eventId,
                "COMMERCE_ORDER_CREATED",
                "order:" + aggregateId + ":created",
                aggregateId,
                "commerce",
                "{\"order_id\":\"" + aggregateId + "\"}",
                status,
                retryCount,
                java.sql.Timestamp.from(now)
        );
        return eventId;
    }

    private String queryStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM outbox_events WHERE id = ?",
                String.class,
                eventId
        );
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
        java.sql.Timestamp ts = jdbcTemplate.queryForObject(
                "SELECT published_at FROM outbox_events WHERE id = ?",
                java.sql.Timestamp.class,
                eventId
        );
        return ts == null ? null : ts.toInstant();
    }

    private String queryLastError(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT last_error FROM outbox_events WHERE id = ?",
                String.class,
                eventId
        );
    }

    @TestConfiguration
    static class OutboxPublisherTestConfig {

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
        public void publish(com.twohands.commerce_service.domain.outbox.OutboxEvent event) {
            if (!shouldSucceed.get()) {
                throw new RuntimeException("broker unavailable");
            }
        }
    }
}
