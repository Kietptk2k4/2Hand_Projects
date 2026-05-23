package com.twohands.notification_service.integration.delivery;

import com.twohands.notification_service.application.delivery.RespectNotificationSettingsCommand;
import com.twohands.notification_service.application.delivery.RespectNotificationSettingsUseCase;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class RespectNotificationSettingsIntegrationTest {

    @Autowired
    private RespectNotificationSettingsUseCase respectNotificationSettingsUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void execute_usesDefaultPolicyWhenSettingMissing() {
        UUID userId = UUID.randomUUID();

        var result = respectNotificationSettingsUseCase.execute(
                new RespectNotificationSettingsCommand(userId, "POST_LIKED")
        );

        assertTrue(result.allowInApp());
        assertTrue(result.allowPush());
        assertFalse(result.allowEmail());
        assertFalse(result.explicitSetting());
    }

    @Test
    void execute_readsExplicitUserPreferencesFromDatabase() {
        UUID userId = UUID.randomUUID();
        insertUserSetting(userId, "POST_LIKED", true, false, false);

        var result = respectNotificationSettingsUseCase.execute(
                new RespectNotificationSettingsCommand(userId, "POST_LIKED")
        );

        assertFalse(result.allowInApp());
        assertTrue(result.allowPush());
        assertFalse(result.allowEmail());
        assertTrue(result.explicitSetting());
    }

    @Test
    void execute_throwsForUnknownEventType() {
        UUID userId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> respectNotificationSettingsUseCase.execute(
                new RespectNotificationSettingsCommand(userId, "UNKNOWN_EVENT")
        ));

        assertEquals(ErrorCode.UNKNOWN_EVENT_TYPE, ex.getErrorCode());
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
}
