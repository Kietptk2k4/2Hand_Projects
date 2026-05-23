package com.twohands.notification_service.integration.worker;

import com.twohands.notification_service.application.worker.MarkNotificationEventFailedCommand;
import com.twohands.notification_service.application.worker.MarkNotificationEventFailedUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
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
class MarkNotificationEventFailedIntegrationTest {

    @Autowired
    private MarkNotificationEventFailedUseCase markNotificationEventFailedUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_persistsFailedStateWithRetryMetadata() {
        UUID eventId = insertProcessingEvent();

        var result = markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "Recipient missing",
                NotificationFailurePolicy.RETRYABLE
        ));

        assertTrue(result.updated());
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(1, queryRetryCount(eventId));
        assertEquals("Recipient missing", queryLastError(eventId));
        assertNotNull(queryLockedAt(eventId));
        assertNull(queryLockedBy(eventId));
    }

    @Test
    void execute_marksPermanentFailureForOpsVisibility() {
        UUID eventId = insertProcessingEvent();

        var result = markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "UNSUPPORTED_EVENT_TYPE",
                NotificationFailurePolicy.PERMANENT
        ));

        assertTrue(result.permanentFailure());
        assertEquals(5, queryRetryCount(eventId));
    }

    @Test
    void execute_doesNotUpdateCompletedEvent() {
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
                NotificationEventStatus.COMPLETED,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        ));

        var result = markNotificationEventFailedUseCase.execute(new MarkNotificationEventFailedCommand(
                eventId,
                "late failure",
                NotificationFailurePolicy.RETRYABLE
        ));

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
                null,
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

    private int queryRetryCount(UUID eventId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM notification_events WHERE id = ?",
                Integer.class,
                eventId
        );
        return count == null ? -1 : count;
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
