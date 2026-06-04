package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.register.PasswordHashingService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerifyEmailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordHashingService passwordHashingService;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM login_logs");
        jdbcTemplate.execute("DELETE FROM refresh_token_sessions");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void validTokenShouldActivatePendingUserAndMarkTokenUsed() throws Exception {
        String rawToken = "123456";
        UUID userId = insertUser("verify_pending@example.com", "PENDING_VERIFICATION", false);
        insertVerificationToken(userId, passwordHashingService.hash(rawToken).value());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(rawToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xac thuc email thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email_verified").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM users WHERE id = ?",
                String.class,
                userId
        );
        Boolean emailVerified = jdbcTemplate.queryForObject(
                "SELECT email_verified FROM users WHERE id = ?",
                Boolean.class,
                userId
        );
        Instant tokenUsedAt = jdbcTemplate.queryForObject(
                "SELECT used_at FROM verification_tokens WHERE user_id = ?",
                Instant.class,
                userId
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );

        assertEquals("ACTIVE", status);
        assertTrue(emailVerified);
        assertNotNull(tokenUsedAt);
        assertEquals(1, outboxCount);
    }

    @Test
    void invalidTokenShouldReturn400WithoutChangingUser() throws Exception {
        UUID userId = insertUser("verify_invalid@example.com", "PENDING_VERIFICATION", false);
        insertVerificationToken(userId, passwordHashingService.hash("111111").value());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("999999"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("token"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_OR_EXPIRED"));

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM users WHERE id = ?",
                String.class,
                userId
        );
        assertEquals("PENDING_VERIFICATION", status);
    }

    @Test
    void alreadyActiveUserWithUsedTokenShouldReturn200Idempotent() throws Exception {
        String rawToken = "654321";
        UUID userId = insertUser("verify_active@example.com", "ACTIVE", true);
        insertUsedVerificationToken(userId, passwordHashingService.hash(rawToken).value());

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(rawToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tai khoan da duoc xac thuc truoc do."))
                .andExpect(jsonPath("$.data.user_id").value(userId.toString()));

        Integer outboxCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);
        assertEquals(0, outboxCount);
    }

    @Test
    void nonSixDigitTokenShouldReturn400Validation() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("abcdef"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void emptyTokenShouldReturn400Validation() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest("  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private UUID insertUser(String email, String status, boolean emailVerified) {
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
                emailVerified,
                false,
                now,
                now
        );
        return userId;
    }

    private void insertVerificationToken(UUID userId, String tokenHash) {
        jdbcTemplate.update(
                """
                        INSERT INTO verification_tokens(id, user_id, token_hash, type, expires_at, used_at, created_at)
                        VALUES (?, ?, ?, 'EMAIL_VERIFY', ?, NULL, ?)
                        """,
                UUID.randomUUID(),
                userId,
                tokenHash,
                java.sql.Timestamp.from(Instant.now().plusSeconds(600)),
                java.sql.Timestamp.from(Instant.now().minusSeconds(60))
        );
    }

    private void insertUsedVerificationToken(UUID userId, String tokenHash) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO verification_tokens(id, user_id, token_hash, type, expires_at, used_at, created_at)
                        VALUES (?, ?, ?, 'EMAIL_VERIFY', ?, ?, ?)
                        """,
                UUID.randomUUID(),
                userId,
                tokenHash,
                java.sql.Timestamp.from(now.plusSeconds(600)),
                java.sql.Timestamp.from(now.minusSeconds(30)),
                java.sql.Timestamp.from(now.minusSeconds(120))
        );
    }

    private Map<String, Object> buildRequest(String token) {
        Map<String, Object> request = new HashMap<>();
        request.put("token", token);
        return request;
    }
}
