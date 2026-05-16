package com.twohands.auth_service.integration.auth;

import com.twohands.auth_service.application.auth.oauth.OAuthLoginCommand;
import com.twohands.auth_service.application.auth.oauth.OAuthLoginResult;
import com.twohands.auth_service.application.auth.oauth.OAuthLoginUseCase;
import com.twohands.auth_service.application.auth.oauth.OAuthProfile;
import com.twohands.auth_service.domain.oauth.OAuthProvider;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class OAuthLoginIntegrationTest {

    @Autowired
    private OAuthLoginUseCase oAuthLoginUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM login_logs");
        jdbcTemplate.execute("DELETE FROM refresh_token_sessions");
        jdbcTemplate.execute("DELETE FROM oauth_accounts");
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void googleOAuthNewUserShouldCreateAllAndIssueTokens() {
        OAuthLoginResult result = oAuthLoginUseCase.execute(new OAuthLoginCommand(
                new OAuthProfile(
                        OAuthProvider.GOOGLE,
                        "google-sub-1",
                        "oauth.new.user@example.com",
                        "OAuth New User",
                        "https://avatar.example.com/a.png"
                ),
                "127.0.0.1",
                "JUnit",
                "device-google"
        ));

        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email_normalized = ?", Integer.class,
                "oauth.new.user@example.com");
        Integer profiles = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_profiles up JOIN users u ON up.user_id = u.id WHERE u.email_normalized = ?",
                Integer.class,
                "oauth.new.user@example.com"
        );
        Integer settings = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_settings us JOIN users u ON us.user_id = u.id WHERE u.email_normalized = ?",
                Integer.class,
                "oauth.new.user@example.com"
        );
        Integer outbox = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_CREATED' AND status = 'PENDING'",
                Integer.class
        );
        Integer sessions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM refresh_token_sessions", Integer.class);
        Integer logs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM login_logs WHERE login_method = 'GOOGLE' AND success = TRUE",
                Integer.class
        );

        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
        assertTrue(result.firstLogin());
        assertEquals(1, users);
        assertEquals(1, profiles);
        assertEquals(1, settings);
        assertEquals(1, outbox);
        assertEquals(1, sessions);
        assertEquals(1, logs);
    }

    @Test
    void facebookOAuthExistingUserShouldNotDuplicateAndShouldLinkProvider() {
        UUID userId = insertUser("oauth.existing@example.com", "ACTIVE");

        OAuthLoginResult result = oAuthLoginUseCase.execute(new OAuthLoginCommand(
                new OAuthProfile(
                        OAuthProvider.FACEBOOK,
                        "fb-user-1",
                        "oauth.existing@example.com",
                        "OAuth Existing",
                        "https://fb-avatar.example.com/a.jpg"
                ),
                "127.0.0.1",
                "JUnit",
                "device-facebook"
        ));

        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email_normalized = ?", Integer.class,
                "oauth.existing@example.com");
        Integer oauthLinks = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oauth_accounts WHERE user_id = ? AND provider = 'FACEBOOK'",
                Integer.class,
                userId
        );
        Integer sessions = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ?", Integer.class,
                userId);
        Integer logs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM login_logs WHERE user_id = ? AND login_method = 'FACEBOOK' AND success = TRUE",
                Integer.class,
                userId
        );

        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
        assertEquals(userId, result.userId());
        assertFalse(result.firstLogin());
        assertEquals(1, users);
        assertEquals(1, oauthLinks);
        assertEquals(1, sessions);
        assertEquals(1, logs);
    }

    @Test
    void oauthSuspendedOrDeletedShouldBlockAndNotCreateSession() {
        UUID suspendedUserId = insertUser("oauth.suspended@example.com", "SUSPENDED");
        UUID deletedUserId = insertUser("oauth.deleted@example.com", "DELETED");

        assertThrows(AppException.class, () -> oAuthLoginUseCase.execute(new OAuthLoginCommand(
                new OAuthProfile(OAuthProvider.GOOGLE, "google-sub-suspend", "oauth.suspended@example.com", "Suspended User", null),
                "127.0.0.1",
                "JUnit",
                "device-suspended"
        )));
        assertThrows(AppException.class, () -> oAuthLoginUseCase.execute(new OAuthLoginCommand(
                new OAuthProfile(OAuthProvider.GOOGLE, "google-sub-deleted", "oauth.deleted@example.com", "Deleted User", null),
                "127.0.0.1",
                "JUnit",
                "device-deleted"
        )));

        Integer suspendedSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ?",
                Integer.class,
                suspendedUserId
        );
        Integer deletedSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ?",
                Integer.class,
                deletedUserId
        );
        assertEquals(0, suspendedSessions);
        assertEquals(0, deletedSessions);
    }

    @Test
    void oauthMissingEmailShouldFail() {
        assertThrows(AppException.class, () -> oAuthLoginUseCase.execute(new OAuthLoginCommand(
                new OAuthProfile(OAuthProvider.GOOGLE, "google-no-email", null, "No Email", null),
                "127.0.0.1",
                "JUnit",
                "device-no-email"
        )));

        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        assertEquals(0, users);
    }

    private UUID insertUser(String email, String status) {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO users(
                            id, email, email_normalized, password_hash, status,
                            email_verified, phone_verified, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                userId,
                email,
                email.toLowerCase(),
                "$2a$10$abcdefghijklmnopqrstuv",
                status,
                true,
                false,
                now,
                now
        );
        return userId;
    }
}
