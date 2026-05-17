package com.twohands.auth_service.integration.useraccount;

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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAccountIntegrationTest {

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
    void viewAccountShouldReturnCombinedData() throws Exception {
        TestUser user = insertFullUser("view_account@example.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.id").value(user.userId().toString()))
                .andExpect(jsonPath("$.data.profile.display_name").value("Initial Name"))
                .andExpect(jsonPath("$.data.settings.appearance_mode").value("SYSTEM"));
    }

    @Test
    void viewSessionsShouldReturnOnlyActiveSessionsSortedDesc() throws Exception {
        TestUser user = insertFullUser("view_sessions@example.com");

        Instant older = Instant.now().minusSeconds(300);
        Instant newer = Instant.now().minusSeconds(120);
        insertSession(user.userId(), "device-old", "10.0.0.1", "ACTIVE", older);
        insertSession(user.userId(), "device-new", "10.0.0.2", "ACTIVE", newer);
        insertSession(user.userId(), "device-revoked", "10.0.0.3", "REVOKED", Instant.now().minusSeconds(30));

        mockMvc.perform(get("/api/v1/users/me/sessions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay danh sach phien dang nhap thanh cong."))
                .andExpect(jsonPath("$.data.sessions.length()").value(2))
                .andExpect(jsonPath("$.data.sessions[0].device_id").value("device-new"))
                .andExpect(jsonPath("$.data.sessions[1].device_id").value("device-old"))
                .andExpect(jsonPath("$.data.sessions[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.sessions[0].token_hash").doesNotExist());
    }

    @Test
    void viewSessionsShouldReturnEmptyListWhenNoActiveSession() throws Exception {
        TestUser user = insertFullUser("view_sessions_empty@example.com");
        insertSession(user.userId(), "device-revoked", "10.10.10.1", "REVOKED", Instant.now().minusSeconds(100));

        mockMvc.perform(get("/api/v1/users/me/sessions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessions.length()").value(0));
    }

    @Test
    void updateProfileShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("update_profile@example.com");

        Map<String, Object> request = new HashMap<>();
        request.put("display_name", "Kiet Tran");
        request.put("bio", "Backend engineer");
        request.put("website", "https://example.com");
        request.put("social_links", Map.of("github", "https://github.com/kiet"));

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String displayName = jdbcTemplate.queryForObject(
                "SELECT display_name FROM user_profiles WHERE user_id = ?",
                String.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals("Kiet Tran", displayName);
        assertEquals(1, outboxCount);
    }

    @Test
    void updateAvatarShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("update_avatar@example.com");

        Map<String, Object> request = Map.of("avatar_url", "https://minio.local/2hands-avatar/u1.png");

        mockMvc.perform(patch("/api/v1/users/me/avatar")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String avatarUrl = jdbcTemplate.queryForObject(
                "SELECT avatar_url FROM user_profiles WHERE user_id = ?",
                String.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals("https://minio.local/2hands-avatar/u1.png", avatarUrl);
        assertEquals(1, outboxCount);
    }

    @Test
    void togglePrivacyShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("toggle_privacy@example.com");

        Map<String, Object> request = Map.of("is_private", true);

        mockMvc.perform(patch("/api/v1/users/me/privacy")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Boolean isPrivate = jdbcTemplate.queryForObject(
                "SELECT is_private FROM user_profiles WHERE user_id = ?",
                Boolean.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals(true, isPrivate);
        assertEquals(1, outboxCount);
    }

    @Test
    void updateSettingsShouldPersistAppearanceMode() throws Exception {
        TestUser user = insertFullUser("update_settings@example.com");

        Map<String, Object> request = Map.of("appearance_mode", "DARK");

        mockMvc.perform(patch("/api/v1/users/me/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.appearance_mode").value("DARK"));

        String appearanceMode = jdbcTemplate.queryForObject(
                "SELECT appearance_mode FROM user_settings WHERE user_id = ?",
                String.class,
                user.userId()
        );
        assertEquals("DARK", appearanceMode);
    }

    @Test
    void softDeleteShouldSetDeletedRevokeSessionsAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("soft_delete@example.com");
        insertActiveSession(user.userId());
        insertActiveSession(user.userId());

        Map<String, Object> request = Map.of("password", "Password123");

        mockMvc.perform(post("/api/v1/users/me/soft-delete")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String statusValue = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, user.userId());
        Integer deletedAtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND deleted_at IS NOT NULL",
                Integer.class,
                user.userId()
        );
        Integer activeSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                user.userId()
        );
        Integer revokedSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'REVOKED'",
                Integer.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_DELETED' AND status = 'PENDING'",
                Integer.class
        );

        assertEquals("DELETED", statusValue);
        assertEquals(1, deletedAtCount);
        assertEquals(0, activeSessions);
        assertEquals(2, revokedSessions);
        assertEquals(1, outboxCount);
    }

    @Test
    void softDeleteWrongPasswordShouldReturn400AndNoChanges() throws Exception {
        TestUser user = insertFullUser("soft_delete_wrong_password@example.com");
        insertActiveSession(user.userId());

        String beforeStatus = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, user.userId());
        Integer beforeOutbox = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);

        mockMvc.perform(post("/api/v1/users/me/soft-delete")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "WrongPassword123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mat khau khong chinh xac."));

        String afterStatus = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, user.userId());
        Integer afterActiveSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                user.userId()
        );
        Integer afterOutbox = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);
        Integer deletedAtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND deleted_at IS NOT NULL",
                Integer.class,
                user.userId()
        );

        assertEquals(beforeStatus, afterStatus);
        assertEquals(1, afterActiveSessions);
        assertEquals(beforeOutbox, afterOutbox);
        assertEquals(0, deletedAtCount);
    }

    @Test
    void missingJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    private TestUser insertFullUser(String email) {
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
                passwordEncoder.encode("Password123"),
                "ACTIVE",
                true,
                false,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        jdbcTemplate.update(
                """
                        INSERT INTO user_profiles(
                            user_id, display_name, avatar_url, bio, website, social_links, is_private, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                userId,
                "Initial Name",
                "https://minio.local/2hands-avatar/default.png",
                "Initial bio",
                "https://example.org",
                "{\"github\":\"https://github.com/initial\"}",
                false,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        jdbcTemplate.update(
                """
                        INSERT INTO user_settings(
                            user_id, appearance_mode, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?)
                        """,
                userId,
                "SYSTEM",
                Timestamp.from(now),
                Timestamp.from(now)
        );
        return new TestUser(userId, email);
    }

    private void insertActiveSession(UUID userId) {
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO refresh_token_sessions(
                            id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                sessionId,
                userId,
                "hash-" + sessionId,
                "device",
                "127.0.0.1",
                "JUnit",
                Timestamp.from(now.plusSeconds(3600)),
                "ACTIVE",
                Timestamp.from(now),
                Timestamp.from(now)
        );
    }

    private void insertSession(UUID userId, String deviceId, String ip, String status, Instant createdAt) {
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
                "hash-" + sessionId,
                deviceId,
                ip,
                "JUnit",
                Timestamp.from(createdAt.plusSeconds(3600)),
                status,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
    }

    private String bearerTokenFor(UUID userId, String email) {
        return "Bearer " + jwtTokenIssuer.issue(userId, email, "ACTIVE", Instant.now()).accessToken();
    }

    private record TestUser(UUID userId, String email) {
    }
}
