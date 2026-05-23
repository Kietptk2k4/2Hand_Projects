package com.twohands.notification_service.integration.delivery;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.worker.ProcessPendingNotificationEventsUseCase;
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
class ApplyNotificationDeliveryRulesIntegrationTest {

    @Autowired
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Autowired
    private ProcessPendingNotificationEventsUseCase processPendingNotificationEventsUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
        jdbcTemplate.execute("DELETE FROM user_device_tokens");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void execute_appliesUserSettingToDisableInAppDelivery() {
        UUID userId = UUID.randomUUID();
        insertUserSetting(userId, "POST_LIKED", true, false, false);

        var decision = applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(userId, "POST_LIKED")
        );

        assertFalse(decision.inApp());
        assertFalse(decision.push());
    }

    @Test
    void execute_skipsPushWhenUserHasNoActiveDeviceToken() {
        UUID userId = UUID.randomUUID();

        var decision = applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(userId, "POST_LIKED")
        );

        assertTrue(decision.inApp());
        assertFalse(decision.push());
    }

    @Test
    void processEvent_respectsDisabledInAppSetting() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        insertUserSetting(recipientId, "POST_LIKED", true, false, false);
        insertPendingEvent(recipientId, actorId);

        int processed = processPendingNotificationEventsUseCase.execute(10);

        assertEquals(1, processed);
        assertEquals("COMPLETED", queryEventStatus());
        assertEquals(0, countUserNotifications());
    }

    private void insertUserSetting(
            UUID userId,
            String eventType,
            boolean allowPush,
            boolean allowEmail,
            boolean allowInApp
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId,
                eventType,
                allowPush,
                allowEmail,
                allowInApp
        );
    }

    private void insertPendingEvent(UUID recipientId, UUID actorId) {
        jdbcTemplate.update(
                """
                        INSERT INTO notification_events(
                            id, source_event_id, event_type, source_service, aggregate_type, aggregate_id,
                            actor_id, recipient_user_id, payload, status, retry_count, max_retry_count, created_at
                        )
                        VALUES (?, ?, 'POST_LIKED', 'SOCIAL', 'POST', 'post-id', ?, ?, '{}', 'PENDING', 0, 5, CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                UUID.randomUUID(),
                actorId,
                recipientId
        );
    }

    private String queryEventStatus() {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events",
                String.class
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
