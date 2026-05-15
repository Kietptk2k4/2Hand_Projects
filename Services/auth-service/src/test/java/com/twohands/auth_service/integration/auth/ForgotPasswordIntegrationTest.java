package com.twohands.auth_service.integration.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForgotPasswordIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void existingEmailShouldCreateVerificationTokenAndOutboxAndReturn200() throws Exception {
        String email = "forgot_exists@example.com";
        UUID userId = insertUser(email);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Neu email hop le, chung toi da gui huong dan dat lai mat khau."));

        Integer tokenCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM verification_tokens WHERE user_id = ? AND type = 'PASSWORD_RESET'",
                Integer.class,
                userId
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'PASSWORD_RESET_REQUESTED' AND status = 'PENDING'",
                Integer.class
        );

        assertEquals(1, tokenCount);
        assertEquals(1, outboxCount);
    }

    @Test
    void nonExistingEmailShouldStillReturn200AndNotCreateTokenOrEvent() throws Exception {
        String email = "forgot_missing@example.com";

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Neu email hop le, chung toi da gui huong dan dat lai mat khau."));

        Integer tokenCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM verification_tokens", Integer.class);
        Integer outboxCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);
        assertEquals(0, tokenCount);
        assertEquals(0, outboxCount);
    }

    @Test
    void invalidPayloadShouldReturn400() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "invalid-email");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private UUID insertUser(String email) {
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
                "ACTIVE",
                true,
                false,
                now,
                now
        );
        return userId;
    }

    private Map<String, Object> buildRequest(String email) {
        Map<String, Object> request = new HashMap<>();
        request.put("email", email);
        return request;
    }
}
