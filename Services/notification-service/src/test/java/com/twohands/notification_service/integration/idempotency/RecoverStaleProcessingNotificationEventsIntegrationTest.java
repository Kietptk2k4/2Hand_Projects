package com.twohands.notification_service.integration.idempotency;

import com.twohands.notification_service.application.idempotency.RecoverStaleProcessingNotificationEventsUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class RecoverStaleProcessingNotificationEventsIntegrationTest {

    @Autowired
    private RecoverStaleProcessingNotificationEventsUseCase recoverStaleProcessingNotificationEventsUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID eventId;

    @BeforeEach
    void cleanTable() {
        jdbcTemplate.execute("DELETE FROM notification_events");
        eventId = UUID.randomUUID();
    }

    @Test
    void execute_recoversStaleProcessingEvent() {
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
                Instant.now().minusSeconds(600),
                "worker-1",
                Instant.now().minusSeconds(900),
                null
        ));

        int recovered = recoverStaleProcessingNotificationEventsUseCase.execute(10, 300);

        assertEquals(1, recovered);
        assertEquals("FAILED", queryStatus(eventId));
        assertEquals(1, queryRetryCount(eventId));
        assertEquals(RecoverStaleProcessingNotificationEventsUseCase.STALE_PROCESSING_ERROR, queryLastError(eventId));
        assertNull(queryLockedAt(eventId));
        assertNull(queryLockedBy(eventId));
    }

    private String queryStatus(UUID id) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                id
        );
    }

    private int queryRetryCount(UUID id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT retry_count FROM notification_events WHERE id = ?",
                Integer.class,
                id
        );
        return count == null ? -1 : count;
    }

    private String queryLastError(UUID id) {
        return jdbcTemplate.queryForObject(
                "SELECT last_error FROM notification_events WHERE id = ?",
                String.class,
                id
        );
    }

    private Object queryLockedAt(UUID id) {
        return jdbcTemplate.queryForObject(
                "SELECT locked_at FROM notification_events WHERE id = ?",
                Object.class,
                id
        );
    }

    private String queryLockedBy(UUID id) {
        return jdbcTemplate.queryForObject(
                "SELECT locked_by FROM notification_events WHERE id = ?",
                String.class,
                id
        );
    }
}
