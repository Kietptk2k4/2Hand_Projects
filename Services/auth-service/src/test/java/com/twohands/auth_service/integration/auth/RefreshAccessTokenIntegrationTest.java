package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshAccessTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TokenHashingService tokenHashingService;

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
    void refreshSuccessShouldReturnNewAccessToken() throws Exception {
        UUID userId = insertUser("refresh_success@example.com", "ACTIVE");
        insertRefreshSession(userId, "raw-refresh-token", "ACTIVE", Instant.now().plusSeconds(3600));

        Map<String, Object> request = buildRequest("raw-refresh-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.expires_in").isNumber());
    }

    @Test
    void invalidTokenShouldReturn401() throws Exception {
        Map<String, Object> request = buildRequest("unknown-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Phien dang nhap khong hop le hoac da het han. Vui long dang nhap lai."));
    }

    @Test
    void expiredSessionShouldReturn401() throws Exception {
        UUID userId = insertUser("refresh_expired@example.com", "ACTIVE");
        insertRefreshSession(userId, "expired-token", "ACTIVE", Instant.now().minusSeconds(5));

        Map<String, Object> request = buildRequest("expired-token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void suspendedAndDeletedUserShouldReturn401() throws Exception {
        UUID suspendedUserId = insertUser("refresh_suspend@example.com", "SUSPENDED");
        insertRefreshSession(suspendedUserId, "suspended-token", "ACTIVE", Instant.now().plusSeconds(3600));

        UUID deletedUserId = insertUser("refresh_deleted@example.com", "DELETED");
        insertRefreshSession(deletedUserId, "deleted-token", "ACTIVE", Instant.now().plusSeconds(3600));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("suspended-token"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("deleted-token"))))
                .andExpect(status().isUnauthorized());
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

    private void insertRefreshSession(UUID userId, String rawRefreshToken, String status, Instant expiresAt) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO refresh_token_sessions(
                            id, user_id, token_hash, device_id, ip_address, user_agent,
                            expires_at, status, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                userId,
                tokenHashingService.sha256(rawRefreshToken),
                "device-test",
                "127.0.0.1",
                "JUnit",
                expiresAt,
                status,
                now,
                now
        );
    }

    private Map<String, Object> buildRequest(String refreshToken) {
        Map<String, Object> request = new HashMap<>();
        request.put("refresh_token", refreshToken);
        return request;
    }
}
