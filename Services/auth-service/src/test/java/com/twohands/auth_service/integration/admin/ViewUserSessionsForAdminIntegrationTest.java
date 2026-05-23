package com.twohands.auth_service.integration.admin;

import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
class ViewUserSessionsForAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenIssuer jwtTokenIssuer;

    @BeforeEach
    void cleanTables() {
        ensureRbacTables();
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
    void shouldReturnActiveSessionsForInvestigationActor() throws Exception {
        UUID actorId = insertUser("investigator@example.com");
        UUID targetUserId = insertUser("target@example.com");
        grantInvestigationPermission(actorId);

        Instant older = Instant.now().minusSeconds(300);
        Instant newer = Instant.now().minusSeconds(120);
        insertSession(targetUserId, "device-old", "ACTIVE", older);
        insertSession(targetUserId, "device-new", "ACTIVE", newer);
        insertSession(targetUserId, "device-revoked", "REVOKED", Instant.now().minusSeconds(30));

        mockMvc.perform(get("/api/v1/admin/users/{userId}/sessions", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "investigator@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay danh sach phien dang nhap thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(targetUserId.toString()))
                .andExpect(jsonPath("$.data.sessions.length()").value(2))
                .andExpect(jsonPath("$.data.sessions[0].device_id").value("device-new"))
                .andExpect(jsonPath("$.data.sessions[1].device_id").value("device-old"))
                .andExpect(jsonPath("$.data.sessions[0].token_hash").doesNotExist())
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.total_items").value(2))
                .andExpect(jsonPath("$.data.pagination.has_next").value(false));
    }

    @Test
    void shouldReturn403WithoutPermission() throws Exception {
        UUID actorId = insertUser("no_perm@example.com");
        UUID targetUserId = insertUser("target2@example.com");

        mockMvc.perform(get("/api/v1/admin/users/{userId}/sessions", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "no_perm@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn404ForMissingUser() throws Exception {
        UUID actorId = insertUser("investigator404@example.com");
        grantInvestigationPermission(actorId);

        mockMvc.perform(get("/api/v1/admin/users/{userId}/sessions", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "investigator404@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}/sessions", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private void grantInvestigationPermission(UUID actorId) {
        UUID roleId = insertRole("SUPPORT", "Support");
        UUID permissionId = insertPermission("USER_INVESTIGATION_READ", "Investigation read");
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

    private void insertSession(UUID userId, String deviceId, String status, Instant createdAt) {
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
                "203.0.113.1",
                "JUnit",
                Timestamp.from(createdAt.plusSeconds(3600)),
                status,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
    }

    private UUID insertRole(String code, String name) {
        UUID roleId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO roles(id, code, name, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                roleId,
                code,
                name,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        return roleId;
    }

    private UUID insertPermission(String code, String description) {
        UUID permissionId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO permissions(id, code, description, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                permissionId,
                code,
                description,
                Timestamp.from(now),
                Timestamp.from(now)
        );
        return permissionId;
    }

    private void insertRolePermission(UUID roleId, UUID permissionId) {
        Instant now = Instant.now();
        jdbcTemplate.update(
                """
                        INSERT INTO role_permissions(role_id, permission_id, created_at, updated_at)
                        VALUES (?, ?, ?, ?)
                        """,
                roleId,
                permissionId,
                Timestamp.from(now),
                Timestamp.from(now)
        );
    }

    private void insertUserRole(UUID userId, UUID roleId) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_roles(user_id, role_id, created_at)
                        VALUES (?, ?, ?)
                        """,
                userId,
                roleId,
                Timestamp.from(Instant.now())
        );
    }

    private void ensureRbacTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                    id UUID PRIMARY KEY,
                    code VARCHAR(100) NOT NULL UNIQUE,
                    name VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS permissions (
                    id UUID PRIMARY KEY,
                    code VARCHAR(100) NOT NULL UNIQUE,
                    description VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS role_permissions (
                    role_id UUID NOT NULL,
                    permission_id UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    PRIMARY KEY (role_id, permission_id)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_roles (
                    user_id UUID NOT NULL,
                    role_id UUID NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    PRIMARY KEY (user_id, role_id)
                )
                """);
    }

    private String bearerTokenFor(UUID userId, String email) {
        return "Bearer " + jwtTokenIssuer.issue(userId, email, "ACTIVE", Instant.now()).accessToken();
    }
}
