package com.twohands.notification_service.integration.worker;

import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.application.worker.ProcessPendingNotificationEventsUseCase;
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

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class ProcessNotificationEventIntegrationTest {

    @Autowired
    private ProcessPendingNotificationEventsUseCase processPendingNotificationEventsUseCase;

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

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
    void execute_createsInAppNotificationAndCompletesEvent() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID eventId = insertPendingEvent(recipientId, actorId);

        int processed = processPendingNotificationEventsUseCase.execute(10);

        assertEquals(1, processed);
        assertEquals("COMPLETED", queryEventStatus(eventId));
        assertNotNull(queryProcessedAt(eventId));
        assertEquals(1, countUserNotifications(eventId, recipientId));
        assertEquals("SENT", queryDeliveryStatus(eventId, recipientId));
    }

    @Test
    void execute_completesEventWithoutNotificationWhenSelfAction() {
        UUID userId = UUID.randomUUID();
        UUID eventId = insertPendingEvent(userId, userId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));
        assertEquals(0, countUserNotifications(eventId, userId));
    }

    @Test
    void execute_marksUnsupportedEventAsFailed() {
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "UNKNOWN_EVENT_TYPE",
                NotificationSourceService.COMMERCE,
                null,
                null,
                null,
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

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
        assertEquals(5, queryRetryCount(eventId));
    }

    private UUID insertPendingEvent(UUID recipientId, UUID actorId) {
        UUID eventId = UUID.randomUUID();
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
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        ));
        return eventId;
    }

    private String queryEventStatus(UUID eventId) {
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

    private String queryDeliveryStatus(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT delivery_status FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }
}
