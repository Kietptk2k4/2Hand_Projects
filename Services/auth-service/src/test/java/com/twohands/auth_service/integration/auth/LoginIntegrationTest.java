package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void loginSuccessActiveUserShouldCreateSessionAndLog() throws Exception {
        UUID userId = insertUser("active_login@example.com", "ACTIVE");

        Map<String, Object> request = buildRequest("active_login@example.com", "Password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "test-device")
                        .header("User-Agent", "JUnit")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").exists())
                .andExpect(jsonPath("$.data.refresh_token").exists())
                .andExpect(jsonPath("$.data.user.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.user.status").value("ACTIVE"));

        Integer sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                userId
        );
        Integer successLogs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM login_logs WHERE user_id = ? AND success = TRUE",
                Integer.class,
                userId
        );
        Integer updatedLastLogin = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND last_login_at IS NOT NULL",
                Integer.class,
                userId
        );

        assertEquals(1, sessionCount);
        assertEquals(1, successLogs);
        assertEquals(1, updatedLastLogin);
    }

    @Test
    void loginWrongPasswordShouldReturn401AndCreateFailureLog() throws Exception {
        UUID userId = insertUser("wrong_password@example.com", "ACTIVE");

        Map<String, Object> request = buildRequest("wrong_password@example.com", "WrongPassword123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email hoac mat khau khong chinh xac."));

        Integer failureLogs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM login_logs WHERE user_id = ? AND success = FALSE",
                Integer.class,
                userId
        );
        Integer sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ?",
                Integer.class,
                userId
        );

        assertEquals(1, failureLogs);
        assertEquals(0, sessionCount);
    }

    @Test
    void loginSuspendedUserShouldReturn403() throws Exception {
        UUID userId = insertUser("suspended_login@example.com", "SUSPENDED");

        Map<String, Object> request = buildRequest("suspended_login@example.com", "Password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        Integer sessionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ?",
                Integer.class,
                userId
        );
        assertEquals(0, sessionCount);
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
                passwordEncoder.encode("Password123"),
                status,
                true,
                false,
                now,
                now
        );
        return userId;
    }

    private Map<String, Object> buildRequest(String email, String password) {
        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);
        return request;
    }
}
