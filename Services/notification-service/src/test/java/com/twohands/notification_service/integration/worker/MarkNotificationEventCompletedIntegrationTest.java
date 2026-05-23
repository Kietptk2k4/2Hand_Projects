package com.twohands.notification_service.integration.worker;

import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventCompletedUseCase;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class MarkNotificationEventCompletedIntegrationTest {

    @Autowired
    private MarkNotificationEventCompletedUseCase markNotificationEventCompletedUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_persistsCompletedStateWithProcessedAt() {
        UUID eventId = insertProcessingEvent();

        var result = markNotificationEventCompletedUseCase.execute(new MarkNotificationEventCompletedCommand(eventId));

        assertTrue(result.updated());
        assertNotNull(result.processedAt());
        assertEquals("COMPLETED", queryStatus(eventId));
        assertNotNull(queryProcessedAt(eventId));
        assertNull(queryLastError(eventId));
        assertNull(queryLockedAt(eventId));
        assertNull(queryLockedBy(eventId));
    }

    @Test
    void execute_doesNotUpdateAlreadyCompletedEvent() {
        UUID eventId = UUID.randomUUID();
        Instant processedAt = Instant.now();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                NotificationEventStatus.COMPLETED,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                processedAt
        ));

        var result = markNotificationEventCompletedUseCase.execute(new MarkNotificationEventCompletedCommand(eventId));

        assertFalse(result.updated());
        assertEquals("COMPLETED", queryStatus(eventId));
    }

    private UUID insertProcessingEvent() {
        UUID eventId = UUID.randomUUID();
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                "retryable error",
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        ));
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

    private String queryLastError(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT last_error FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }

    private Object queryLockedAt(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT locked_at FROM notification_events WHERE id = ?",
                Object.class,
                eventId
        );
    }

    private String queryLockedBy(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT locked_by FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
