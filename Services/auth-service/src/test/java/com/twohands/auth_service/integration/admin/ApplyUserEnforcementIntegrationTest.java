package com.twohands.auth_service.integration.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.admin.applyuserenforcement.ConsumeUserEnforcementEventCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ConsumeUserEnforcementEventUseCase;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplyUserEnforcementIntegrationTest {

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

    @Autowired
    private ConsumeUserEnforcementEventUseCase consumeUserEnforcementEventUseCase;

    @BeforeEach
    void cleanTables() {
        ensureRbacTables();
        jdbcTemplate.execute("DELETE FROM user_enforcement_snapshots");
        jdbcTemplate.execute("DELETE FROM refresh_token_sessions");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM role_permissions");
        jdbcTemplate.execute("DELETE FROM permissions");
        jdbcTemplate.execute("DELETE FROM roles");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void suspendApiShouldPersistSnapshotAndRevokeSessions() throws Exception {
        UUID actorId = insertUser("admin_suspend@example.com");
        UUID targetUserId = insertUser("target_suspend@example.com");
        grantPermission(actorId, "USER_SUSPEND");
        insertActiveSession(targetUserId);
        UUID enforcementId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/users/{userId}/suspend", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "enforcement_id", enforcementId.toString(),
                                "reason_code", "ABUSE",
                                "description", "Spam content"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        String userStatus = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, targetUserId);
        Integer activeSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                targetUserId
        );
        Integer snapshots = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_enforcement_snapshots WHERE enforcement_id = ? AND status = 'APPLIED'",
                Integer.class,
                enforcementId
        );

        assertThat(userStatus).isEqualTo("SUSPENDED");
        assertThat(activeSessions).isZero();
        assertThat(snapshots).isEqualTo(1);
    }

    @Test
    void eventConsumerShouldBeIdempotentByEventId() {
        UUID userId = insertUser("event_user@example.com");
        UUID enforcementId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        var first = consumeUserEnforcementEventUseCase.execute(new ConsumeUserEnforcementEventCommand(
                eventId,
                "USER_SUSPENDED",
                enforcementId,
                userId,
                "SUSPEND",
                "ABUSE",
                "Spam",
                null,
                Instant.now()
        ));
        var second = consumeUserEnforcementEventUseCase.execute(new ConsumeUserEnforcementEventCommand(
                eventId,
                "USER_SUSPENDED",
                enforcementId,
                userId,
                "SUSPEND",
                "ABUSE",
                "Spam",
                null,
                Instant.now()
        ));

        assertThat(first.idempotentReplay()).isFalse();
        assertThat(second.idempotentReplay()).isTrue();
        Integer snapshotCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_enforcement_snapshots WHERE event_id = ?",
                Integer.class,
                eventId
        );
        assertThat(snapshotCount).isEqualTo(1);
    }

    private void grantPermission(UUID actorId, String permissionCode) {
        UUID roleId = insertRole("ADMIN", "Admin");
        UUID permissionId = insertPermission(permissionCode, permissionCode);
        insertRolePermission(roleId, permissionId);
        insertUserRole(actorId, roleId);
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
                        VALUES (?, ?, ?, ?, 'ACTIVE', true, false, ?, ?)
                        """,
                userId,
                email,
                email.toLowerCase(),
                passwordEncoder.encode("Password123!"),
                Timestamp.from(now),
                Timestamp.from(now)
        );
        jdbcTemplate.update(
                """
                        INSERT INTO user_profiles(
                            user_id, display_name, avatar_url, bio, website, social_links, is_private, created_at, updated_at
                        )
                        VALUES (?, 'User', NULL, NULL, NULL, NULL, false, ?, ?)
                        """,
                userId,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        jdbcTemplate.update(
                """
                        INSERT INTO user_settings(user_id, appearance_mode, created_at, updated_at)
                        VALUES (?, 'SYSTEM', ?, ?)
                        """,
                userId,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        return userId;
    }

    private void insertActiveSession(UUID userId) {
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO refresh_token_sessions(
                            id, user_id, token_hash, device_id, ip_address, user_agent, expires_at, status, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?)
                        """,
                sessionId,
                userId,
                "hash-" + sessionId,
                "device",
                "127.0.0.1",
                "JUnit",
                Timestamp.from(now.plusSeconds(3600)),
                Timestamp.from(now),
                Timestamp.from(now)
        );
    }

    private UUID insertRole(String code, String name) {
        UUID roleId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                "INSERT INTO roles(id, code, name, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                roleId, code, name, Timestamp.from(now), Timestamp.from(now)
        );
        return roleId;
    }

    private UUID insertPermission(String code, String description) {
        UUID permissionId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                "INSERT INTO permissions(id, code, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                permissionId, code, description, Timestamp.from(now), Timestamp.from(now)
        );
        return permissionId;
    }

    private void insertRolePermission(UUID roleId, UUID permissionId) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                "INSERT INTO role_permissions(role_id, permission_id, created_at, updated_at) VALUES (?, ?, ?, ?)",
                roleId, permissionId, Timestamp.from(now), Timestamp.from(now)
        );
    }

    private void insertUserRole(UUID userId, UUID roleId) {
        jdbcTemplate.update(
                "INSERT INTO user_roles(user_id, role_id, created_at) VALUES (?, ?, ?)",
                userId, roleId, Timestamp.from(Instant.now())
        );
    }

    private void ensureRbacTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    id UUID PRIMARY KEY, code VARCHAR(100) NOT NULL UNIQUE, name VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS permissions (
                    id UUID PRIMARY KEY, code VARCHAR(100) NOT NULL UNIQUE, description VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS role_permissions (
                    role_id UUID NOT NULL, permission_id UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL, PRIMARY KEY (role_id, permission_id))
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_roles (
                    user_id UUID NOT NULL, role_id UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL, PRIMARY KEY (user_id, role_id))
                """);
    }

    private String bearerTokenFor(UUID userId) {
        return "Bearer " + jwtTokenIssuer.issue(userId, "admin@example.com", "ACTIVE", Instant.now()).accessToken();
    }
}
