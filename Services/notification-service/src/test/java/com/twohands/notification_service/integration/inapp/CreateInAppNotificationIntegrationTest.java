package com.twohands.notification_service.integration.inapp;

import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CreateInAppNotificationIntegrationTest {

    @Autowired
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

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
    void execute_persistsUnreadInAppNotificationWithSentStatus() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        var result = createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                eventId,
                userId,
                actorId,
                "POST_LIKED",
                "POST",
                "post-1",
                "{\"postId\":\"post-1\"}"
        ));

        assertFalse(result.duplicate());

        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM user_notifications
                        WHERE id = ? AND user_id = ? AND notification_event_id = ?
                        AND type = 'POST_LIKED' AND title = 'Thích bài viết'
                        AND is_read = FALSE AND is_deleted = FALSE
                        AND delivery_status = 'SENT'
                        """,
                Integer.class,
                result.userNotificationId(),
                userId,
                eventId
        );
        assertEquals(1, count);
    }

    @Test
    void execute_createsFromPersistedNotificationEvent() {
        UUID eventId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
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
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        ));

        NotificationEvent event = notificationEventRepository.findById(eventId).orElseThrow();

        var result = createInAppNotificationUseCase.execute(new CreateInAppNotificationCommand(
                event.id(),
                recipientId,
                event.actorId(),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.payload()
        ));

        assertFalse(result.duplicate());
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notifications WHERE notification_event_id = ?",
                Integer.class,
                eventId
        ));
    }

    @Test
    void execute_treatsDuplicateInsertAsSuccess() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        var command = new CreateInAppNotificationCommand(
                eventId,
                userId,
                actorId,
                "POST_LIKED",
                "POST",
                "post-1",
                "{}"
        );

        var first = createInAppNotificationUseCase.execute(command);
        var second = createInAppNotificationUseCase.execute(command);

        assertFalse(first.duplicate());
        assertTrue(second.duplicate());
        assertEquals(first.userNotificationId(), second.userNotificationId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notifications WHERE notification_event_id = ?",
                Integer.class,
                eventId
        );
        assertEquals(1, count);
    }
}
