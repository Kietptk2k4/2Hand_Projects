package com.twohands.auth_service.integration.useraccount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicUserProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void publicUserShouldReturnFullFields() throws Exception {
        UUID userId = insertUserWithProfile("public.user@example.com", "ACTIVE", false);

        mockMvc.perform(get("/api/v1/users/{userId}/public-profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay public profile thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.data.display_name").value("Public User"))
                .andExpect(jsonPath("$.data.bio").value("Public bio"))
                .andExpect(jsonPath("$.data.website").value("https://example.com"))
                .andExpect(jsonPath("$.data.social_links.github").value("https://github.com/public-user"))
                .andExpect(jsonPath("$.data.is_private").value(false));
    }

    @Test
    void privateUserShouldMaskFields() throws Exception {
        UUID userId = insertUserWithProfile("private.user@example.com", "ACTIVE", true);

        mockMvc.perform(get("/api/v1/users/{userId}/public-profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_id").value(userId.toString()))
                .andExpect(jsonPath("$.data.display_name").value("Public User"))
                .andExpect(jsonPath("$.data.bio").isEmpty())
                .andExpect(jsonPath("$.data.website").isEmpty())
                .andExpect(jsonPath("$.data.social_links").isMap())
                .andExpect(jsonPath("$.data.social_links").isEmpty())
                .andExpect(jsonPath("$.data.is_private").value(true));
    }

    @Test
    void userNotFoundShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/public-profile", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deletedUserShouldReturn404() throws Exception {
        UUID userId = insertUserWithProfile("deleted.user@example.com", "DELETED", false);

        mockMvc.perform(get("/api/v1/users/{userId}/public-profile", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void invalidUserIdShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/users/not-a-uuid/public-profile"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("userId"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));
    }

    private UUID insertUserWithProfile(String email, String status, boolean isPrivate) {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO users(
                            id, email, email_normalized, password_hash, status,
                            email_verified, phone_verified, created_at, updated_at, deleted_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                userId,
                email,
                email.toLowerCase(),
                passwordEncoder.encode("Password123"),
                status,
                true,
                false,
                Timestamp.from(now),
                Timestamp.from(now),
                "DELETED".equals(status) ? Timestamp.from(now) : null
        );

        jdbcTemplate.update(
                """
                        INSERT INTO user_profiles(
                            user_id, display_name, avatar_url, bio, website, social_links, is_private, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                userId,
                "Public User",
                "https://minio.local/2hands-avatar/public-user.png",
                "Public bio",
                "https://example.com",
                "{\"github\":\"https://github.com/public-user\"}",
                isPrivate,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        return userId;
    }
}
