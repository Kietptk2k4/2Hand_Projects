package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
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

@SpringBootTest
@ActiveProfiles("test")
class AuthUserLifecycleNotificationIntegrationTest {

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
    void processUserUpdatedEvent_completesWithoutCreatingUserNotification() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        persistProcessingEvent(eventId, "USER_UPDATED", userId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));
        assertEquals(0, countUserNotifications());
    }

    @Test
    void processUserDeletedEvent_completesWithoutCreatingUserNotification() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        persistProcessingEvent(eventId, "USER_DELETED", userId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("COMPLETED", queryEventStatus(eventId));
        assertEquals(0, countUserNotifications());
    }

    private void persistProcessingEvent(UUID eventId, String eventType, UUID userId) {
        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                "{\"user_id\":\"" + userId + "\"}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        ));
    }

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }

    private int countUserNotifications() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notifications",
                Integer.class
        );
        return count == null ? 0 : count;
    }
}
