package com.twohands.notification_service.integration.worker;

import com.twohands.notification_service.application.worker.MarkNotificationEventFailedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.application.worker.RetryFailedNotificationEventsUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class RetryFailedNotificationEventsIntegrationTest {

    @Autowired
    private RetryFailedNotificationEventsUseCase retryFailedNotificationEventsUseCase;

    @Autowired
    private MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_retriesEligibleFailedEventAndCompletes() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertFailedEvent(recipientId, actorId, Instant.now().minusSeconds(120));

        int processed = retryFailedNotificationEventsUseCase.execute(10);

        assertEquals(1, processed);
        assertEquals("COMPLETED", queryStatus(eventId));
        assertNotNull(queryProcessedAt(eventId));
        assertEquals(1, countUserNotifications(eventId, recipientId));
    }

    @Test
    void execute_skipsFailedEventStillWithinBackoffWindow() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertFailedEvent(recipientId, actorId, Instant.now().minusSeconds(5));

        int processed = retryFailedNotificationEventsUseCase.execute(10);

        assertEquals(0, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(1, queryRetryCount(eventId));
    }

    @Test
    void execute_doesNotRetryPermanentFailure() {
        UUID eventId = insertProcessingEvent();

        markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "UNSUPPORTED_EVENT_TYPE",
                NotificationFailurePolicy.PERMANENT
        ));

        int processed = retryFailedNotificationEventsUseCase.execute(10);

        assertEquals(0, processed);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(5, queryRetryCount(eventId));
    }

    @Test
    void execute_isIdempotentWhenUserNotificationAlreadyExists() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertFailedEvent(recipientId, actorId, Instant.now().minusSeconds(120));

        assertEquals(1, retryFailedNotificationEventsUseCase.execute(10));
        assertEquals(1, countUserNotifications(eventId, recipientId));

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                actorId,
                recipientId,
                "{}",
                NotificationEventStatus.FAILED,
                1,
                5,
                "retryable",
                Instant.now().minusSeconds(120),
                null,
                Instant.now(),
                null
        ));

        assertEquals(1, retryFailedNotificationEventsUseCase.execute(10));
        assertEquals("COMPLETED", queryStatus(eventId));
        assertEquals(1, countUserNotifications(eventId, recipientId));
    }

    private UUID insertProcessingEvent() {
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "{}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        ));
        return eventId;
    }

    private UUID insertFailedEvent(UUID recipientId, UUID actorId, Instant failedAt) {
        UUID eventId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO notification_events(
                            id, source_event_id, event_type, source_service, aggregate_type, aggregate_id,
                            actor_id, recipient_user_id, payload, status, retry_count, max_retry_count,
                            last_error, locked_at, created_at
                        )
                        VALUES (?, ?, 'POST_LIKED', 'SOCIAL', 'POST', 'post-id', ?, ?, '{}', 'FAILED', 1, 5,
                                'Recipient missing', ?, CURRENT_TIMESTAMP)
                        """,
                eventId,
                UUID.randomUUID(),
                actorId,
                recipientId,
                Timestamp.from(failedAt)
        );
        return eventId;
    }

    private String queryStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }

    private Object queryProcessedAt(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT processed_at FROM notification_events WHERE id = ?",
                Object.class,
                eventId
        );
    }

    private int queryRetryCount(UUID eventId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM notification_events WHERE id = ?",
                Integer.class,
                eventId
        );
        return count == null ? 0 : count;
    }

    private int countUserNotifications(UUID eventId, UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                Integer.class,
                eventId,
                userId
        );
        return count == null ? 0 : count;
    }
}
