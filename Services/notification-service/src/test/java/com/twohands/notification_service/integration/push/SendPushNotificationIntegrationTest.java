package com.twohands.notification_service.integration.push;

import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "notification.integrations.fcm.enabled=true")
class SendPushNotificationIntegrationTest {

    @Autowired
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_device_tokens");
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void execute_sendsPushWhenEnabledAndActiveTokenPresent() {
        UUID userId = UUID.randomUUID();
        insertActiveToken(userId, "fcm-valid-token");

        var result = sendPushNotificationUseCase.execute(new SendPushNotificationCommand(
                userId,
                "POST_LIKED",
                "POST",
                "post-1",
                UUID.randomUUID()
        ));

        assertEquals(SendPushNotificationOutcome.SENT, result.outcome());
        assertEquals(1, result.sentTokenCount());
    }

    @Test
    void execute_skipsWhenNoActiveDeviceTokens() {
        UUID userId = UUID.randomUUID();

        var result = sendPushNotificationUseCase.execute(new SendPushNotificationCommand(
                userId,
                "POST_LIKED",
                "POST",
                "post-1",
                UUID.randomUUID()
        ));

        assertEquals(SendPushNotificationOutcome.SKIPPED, result.outcome());
    }

    @Test
    void execute_deactivatesInvalidToken() {
        UUID userId = UUID.randomUUID();
        insertActiveToken(userId, "prefix-invalid-token-suffix");

        var result = sendPushNotificationUseCase.execute(new SendPushNotificationCommand(
                userId,
                "PAYMENT_FAILED",
                "PAYMENT",
                "pay-1",
                UUID.randomUUID()
        ));

        assertEquals(SendPushNotificationOutcome.SKIPPED, result.outcome());
        assertFalse(queryIsActive("prefix-invalid-token-suffix"));
    }

    @Test
    void execute_returnsRetryableFailureForRetryableProviderError() {
        UUID userId = UUID.randomUUID();
        insertActiveToken(userId, "device-retryable-token-xyz");

        var result = sendPushNotificationUseCase.execute(new SendPushNotificationCommand(
                userId,
                "ORDER_CREATED",
                "ORDER",
                "ord-1",
                UUID.randomUUID()
        ));

        assertEquals(SendPushNotificationOutcome.FAILED, result.outcome());
        assertEquals("FCM provider timeout.", result.failureReason());
    }

    @Test
    void execute_respectsDisabledPushSettingUnlessCriticalOverride() {
        UUID userId = UUID.randomUUID();
        insertActiveToken(userId, "fcm-token-settings");
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'POST_LIKED', FALSE, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId
        );

        var postLikedResult = sendPushNotificationUseCase.execute(new SendPushNotificationCommand(
                userId,
                "POST_LIKED",
                "POST",
                "post-1",
                UUID.randomUUID()
        ));
        assertEquals(SendPushNotificationOutcome.SKIPPED, postLikedResult.outcome());

        var suspendedResult = sendPushNotificationUseCase.execute(new SendPushNotificationCommand(
                userId,
                "USER_SUSPENDED",
                "USER",
                userId.toString(),
                UUID.randomUUID()
        ));
        assertEquals(SendPushNotificationOutcome.SENT, suspendedResult.outcome());
    }

    private void insertActiveToken(UUID userId, String deviceToken) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, created_at
                        )
                        VALUES (?, ?, 'ANDROID', ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                userId,
                deviceToken
        );
    }

    private boolean queryIsActive(String deviceToken) {
        Boolean active = jdbcTemplate.queryForObject(
                "SELECT is_active FROM user_device_tokens WHERE device_token = ?",
                Boolean.class,
                deviceToken
        );
        return Boolean.TRUE.equals(active);
    }
}
