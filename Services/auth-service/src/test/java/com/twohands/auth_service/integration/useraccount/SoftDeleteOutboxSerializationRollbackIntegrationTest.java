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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SoftDeleteOutboxSerializationRollbackIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM refresh_token_sessions");
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void softDeleteShouldRollbackWhenOutboxSerializationFails() throws Exception {
        TestUser user = insertFullUser("soft_delete_rollback@example.com");
        insertActiveSession(user.userId());

        mockMvc.perform(post("/api/v1/users/me/soft-delete")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "Password123"))))
                .andExpect(status().isInternalServerError());

        String statusValue = jdbcTemplate.queryForObject(
                "SELECT status FROM users WHERE id = ?",
                String.class,
                user.userId()
        );
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
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_DELETED'",
                Integer.class
        );

        assertEquals("ACTIVE", statusValue);
        assertEquals(0, deletedAtCount);
        assertEquals(1, activeSessions);
        assertEquals(0, revokedSessions);
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
                public com.twohands.auth_service.domain.outbox.OutboxEvent userDeleted(UUID userId, String email, Instant now) {
                    throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize outbox payload");
                }
            };
        }
    }
}
