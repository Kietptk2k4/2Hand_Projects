package com.twohands.notification_service.integration.idempotency;

import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CreateIdempotentUserNotificationIntegrationTest {

    @Autowired
    private CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_createsOnlyOneUserNotificationPerIdempotencyKey() {
        UUID eventId = insertNotificationEvent();
        UUID userId = UUID.randomUUID();

        var first = createIdempotentUserNotificationUseCase.execute(command(eventId, userId));
        var second = createIdempotentUserNotificationUseCase.execute(command(eventId, userId));

        assertFalse(first.duplicate());
        assertTrue(second.duplicate());
        assertEquals(first.userNotificationId(), second.userNotificationId());
        assertEquals(1, countUserNotifications(eventId, userId));
    }

    private CreateIdempotentUserNotificationCommand command(UUID eventId, UUID userId) {
        return new CreateIdempotentUserNotificationCommand(
                eventId,
                userId,
                UUID.randomUUID(),
                "POST_LIKED",
                "Someone liked your post",
                "Alice liked your post",
                "POST",
                "post-id",
                "{}"
        );
    }

    private UUID insertNotificationEvent() {
        UUID eventId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO notification_events(
                            id, source_event_id, event_type, source_service, payload,
                            status, retry_count, max_retry_count, created_at
                        )
                        VALUES (?, ?, 'POST_LIKED', 'SOCIAL', '{}', 'PENDING', 0, 5, CURRENT_TIMESTAMP)
                        """,
                eventId,
                UUID.randomUUID()
        );
        return eventId;
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
