package com.twohands.notification_service.integration.devicetoken;

import com.twohands.notification_service.application.devicetoken.CleanupInvalidDeviceTokenUseCase;
import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenCommand;
import com.twohands.notification_service.application.devicetoken.DeactivateInvalidDeviceTokenUseCase;
import com.twohands.notification_service.domain.devicetoken.DeactivateInvalidDeviceTokenOutcome;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "notification.workers.cleanup-invalid-device-tokens.stale-inactive-days=7"
})
class CleanupInvalidDeviceTokenIntegrationTest {

    @Autowired
    private CleanupInvalidDeviceTokenUseCase cleanupInvalidDeviceTokenUseCase;

    @Autowired
    private DeactivateInvalidDeviceTokenUseCase deactivateInvalidDeviceTokenUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_device_tokens");
    }

    @Test
    void execute_deactivatesStaleNeverUsedTokens() {
        insertStaleToken("stale-never-used-token");
        insertRecentActiveToken("recent-active-token");

        int deactivated = cleanupInvalidDeviceTokenUseCase.execute(50);

        assertEquals(1, deactivated);
        assertFalse(queryIsActive("stale-never-used-token"));
        assertTrue(queryIsActive("recent-active-token"));
    }

    @Test
    void execute_isIdempotentOnSecondRun() {
        insertStaleToken("stale-token-idempotent");

        assertEquals(1, cleanupInvalidDeviceTokenUseCase.execute(50));
        assertEquals(0, cleanupInvalidDeviceTokenUseCase.execute(50));
        assertFalse(queryIsActive("stale-token-idempotent"));
    }

    @Test
    void execute_ignoresTokensWithLastUsedAt() {
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active,
                            updated_at, last_used_at, created_at
                        )
                        VALUES (?, ?, 'ANDROID', ?, TRUE, TIMESTAMP '2020-01-01 00:00:00',
                                TIMESTAMP '2020-06-01 00:00:00', CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                userId,
                "used-but-old-token"
        );

        assertEquals(0, cleanupInvalidDeviceTokenUseCase.execute(50));
        assertTrue(queryIsActive("used-but-old-token"));
    }

    @Test
    void deactivateByToken_isSafeWhenTokenNotFound() {
        var result = deactivateInvalidDeviceTokenUseCase.execute(
                new DeactivateInvalidDeviceTokenCommand("non-existent-token-xyz")
        );

        assertEquals(DeactivateInvalidDeviceTokenOutcome.NOT_FOUND, result.outcome());
    }

    private void insertStaleToken(String deviceToken) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, created_at
                        )
                        VALUES (?, ?, 'ANDROID', ?, TRUE, TIMESTAMP '2020-01-01 00:00:00', CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                UUID.randomUUID(),
                deviceToken
        );
    }

    private void insertRecentActiveToken(String deviceToken) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, created_at
                        )
                        VALUES (?, ?, 'ANDROID', ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                UUID.randomUUID(),
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
