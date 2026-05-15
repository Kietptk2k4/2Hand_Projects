package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChangePasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenIssuer jwtTokenIssuer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM login_logs");
        jdbcTemplate.execute("DELETE FROM refresh_token_sessions");
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void missingJwtShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(
                                "OldPassword123",
                                "NewPassword123",
                                "NewPassword123"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void successfulChangeShouldUpdatePasswordAndPasswordChangedAt() throws Exception {
        TestUser testUser = insertUserWithActiveSessions(1);
        String oldPasswordHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?",
                String.class,
                testUser.userId()
        );

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(testUser.userId(), testUser.email()))
                        .content(objectMapper.writeValueAsString(request(
                                "OldPassword123",
                                "NewPassword123",
                                "NewPassword123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Doi mat khau thanh cong."));

        String newPasswordHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?",
                String.class,
                testUser.userId()
        );
        Integer changedAtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND password_changed_at IS NOT NULL",
                Integer.class,
                testUser.userId()
        );

        assertNotEquals(oldPasswordHash, newPasswordHash);
        assertEquals(1, changedAtCount);
    }

    @Test
    void successfulChangeShouldRevokeAllActiveRefreshSessions() throws Exception {
        TestUser testUser = insertUserWithActiveSessions(3);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(testUser.userId(), testUser.email()))
                        .content(objectMapper.writeValueAsString(request(
                                "OldPassword123",
                                "AnotherNew123",
                                "AnotherNew123"
                        ))))
                .andExpect(status().isOk());

        Integer activeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                testUser.userId()
        );
        Integer revokedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'REVOKED'",
                Integer.class,
                testUser.userId()
        );

        assertEquals(0, activeCount);
        assertEquals(3, revokedCount);
    }

    @Test
    void successfulChangeShouldCreatePasswordChangedOutboxEvent() throws Exception {
        TestUser testUser = insertUserWithActiveSessions(1);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(testUser.userId(), testUser.email()))
                        .content(objectMapper.writeValueAsString(request(
                                "OldPassword123",
                                "NewestPassword123",
                                "NewestPassword123"
                        ))))
                .andExpect(status().isOk());

        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'PASSWORD_CHANGED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals(1, outboxCount);
    }

    @Test
    void wrongCurrentPasswordShouldReturn400AndKeepDataUnchanged() throws Exception {
        TestUser testUser = insertUserWithActiveSessions(2);
        String beforePasswordHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?",
                String.class,
                testUser.userId()
        );
        Integer beforeActiveCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                testUser.userId()
        );
        Integer beforeOutboxCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(testUser.userId(), testUser.email()))
                        .content(objectMapper.writeValueAsString(request(
                                "WrongPassword123",
                                "ValidNewPass123",
                                "ValidNewPass123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mat khau hien tai khong chinh xac."));

        String afterPasswordHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?",
                String.class,
                testUser.userId()
        );
        Integer afterActiveCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                testUser.userId()
        );
        Integer afterOutboxCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);
        Integer passwordChangedAtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND password_changed_at IS NOT NULL",
                Integer.class,
                testUser.userId()
        );

        assertEquals(beforePasswordHash, afterPasswordHash);
        assertEquals(beforeActiveCount, afterActiveCount);
        assertEquals(beforeOutboxCount, afterOutboxCount);
        assertEquals(0, passwordChangedAtCount);
    }

    private TestUser insertUserWithActiveSessions(int sessionCount) {
        UUID userId = UUID.randomUUID();
        String email = "change_password_" + userId + "@example.com";
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
                passwordEncoder.encode("OldPassword123"),
                "ACTIVE",
                true,
                false,
                now,
                now
        );

        for (int i = 0; i < sessionCount; i++) {
            UUID sessionId = UUID.randomUUID();
            jdbcTemplate.update(
                    """
                            INSERT INTO refresh_token_sessions(
                                id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    sessionId,
                    userId,
                    "session-token-hash-" + sessionId,
                    "device-" + i,
                    "127.0.0.1",
                    "JUnit",
                    now.plusSeconds(3600),
                    "ACTIVE",
                    now,
                    now
            );
        }

        return new TestUser(userId, email);
    }

    private String bearerTokenFor(UUID userId, String email) {
        String token = jwtTokenIssuer.issue(userId, email, "ACTIVE", Instant.now()).accessToken();
        return "Bearer " + token;
    }

    private Map<String, Object> request(String currentPassword, String newPassword, String confirmNewPassword) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("current_password", currentPassword);
        payload.put("new_password", newPassword);
        payload.put("confirm_new_password", confirmNewPassword);
        return payload;
    }

    private record TestUser(UUID userId, String email) {
    }
}
