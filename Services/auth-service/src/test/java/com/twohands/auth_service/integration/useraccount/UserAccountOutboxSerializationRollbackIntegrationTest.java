package com.twohands.auth_service.integration.useraccount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.useraccount.common.UserAccountOutboxService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAccountOutboxSerializationRollbackIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void updateProfileShouldRollbackWhenOutboxSerializationFails() throws Exception {
        TestUser user = insertFullUser("rollback_profile@example.com");

        Map<String, Object> request = new HashMap<>();
        request.put("display_name", "Updated Name");
        request.put("bio", "Updated bio");
        request.put("website", "https://example.com");
        request.put("social_links", Map.of("github", "https://github.com/updated"));

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        String displayName = jdbcTemplate.queryForObject(
                "SELECT display_name FROM user_profiles WHERE user_id = ?",
                String.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED'",
                Integer.class
        );

        assertEquals("Initial Name", displayName);
        assertEquals(0, outboxCount);
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

    private String bearerTokenFor(UUID userId, String email) {
        return "Bearer " + jwtTokenIssuer.issue(userId, email, "ACTIVE", Instant.now()).accessToken();
    }

    private record TestUser(UUID userId, String email) {
    }

    @TestConfiguration
    static class FailingOutboxServiceConfig {
        @Bean
        @Primary
        UserAccountOutboxService userAccountOutboxService() {
            return new UserAccountOutboxService(new ObjectMapper()) {
                @Override
                public com.twohands.auth_service.domain.outbox.OutboxEvent userUpdated(
                        UUID userId,
                        String email,
                        Instant now,
                        com.twohands.auth_service.application.useraccount.common.UserProjectionSyncPayload sync
                ) {
                    throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload");
                }
            };
        }
    }
}
