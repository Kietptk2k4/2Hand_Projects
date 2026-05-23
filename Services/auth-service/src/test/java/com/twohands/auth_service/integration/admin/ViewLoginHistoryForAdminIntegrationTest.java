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
class ViewLoginHistoryForAdminIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM login_logs");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM role_permissions");
        jdbcTemplate.execute("DELETE FROM permissions");
        jdbcTemplate.execute("DELETE FROM roles");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void shouldReturnLoginHistoryForInvestigationActor() throws Exception {
        UUID actorId = insertUser("investigator@example.com");
        UUID targetUserId = insertUser("target@example.com");
        grantInvestigationPermission(actorId);

        insertLoginLog(targetUserId, "EMAIL", "1.1.1.1", "Agent A", true, Instant.now().minusSeconds(30));
        insertLoginLog(targetUserId, "GOOGLE", "1.1.1.2", "Agent B", false, Instant.now().minusSeconds(20));
        insertLoginLog(targetUserId, "FACEBOOK", "1.1.1.3", "Agent C", true, Instant.now().minusSeconds(10));

        mockMvc.perform(get("/api/v1/admin/users/{userId}/login-history", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "investigator@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay lich su dang nhap thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(targetUserId.toString()))
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.items[0].login_method").value("FACEBOOK"))
                .andExpect(jsonPath("$.data.items[1].login_method").value("GOOGLE"))
                .andExpect(jsonPath("$.data.items[0].id").doesNotExist())
                .andExpect(jsonPath("$.data.pagination.total_items").value(3))
                .andExpect(jsonPath("$.data.pagination.total_pages").value(1))
                .andExpect(jsonPath("$.data.pagination.has_next").value(false));
    }

    @Test
    void shouldFilterBySuccess() throws Exception {
        UUID actorId = insertUser("investigator_filter@example.com");
        UUID targetUserId = insertUser("target_filter@example.com");
        grantInvestigationPermission(actorId);

        insertLoginLog(targetUserId, "EMAIL", "1.1.1.1", "Agent A", true, Instant.now().minusSeconds(30));
        insertLoginLog(targetUserId, "EMAIL", "1.1.1.2", "Agent B", false, Instant.now().minusSeconds(20));

        mockMvc.perform(get("/api/v1/admin/users/{userId}/login-history", targetUserId)
                        .param("success", "true")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "investigator_filter@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].success").value(true))
                .andExpect(jsonPath("$.data.pagination.total_items").value(1));
    }

    @Test
    void shouldReturn403WithoutPermission() throws Exception {
        UUID actorId = insertUser("no_perm_history@example.com");
        UUID targetUserId = insertUser("target_history@example.com");

        mockMvc.perform(get("/api/v1/admin/users/{userId}/login-history", targetUserId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "no_perm_history@example.com")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn404ForMissingUser() throws Exception {
        UUID actorId = insertUser("investigator404_history@example.com");
        grantInvestigationPermission(actorId);

        mockMvc.perform(get("/api/v1/admin/users/{userId}/login-history", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actorId, "investigator404_history@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/{userId}/login-history", UUID.randomUUID()))
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

    private void insertLoginLog(UUID userId, String loginMethod, String ipAddress, String userAgent, boolean success, Instant createdAt) {
        jdbcTemplate.update(
                """
                        INSERT INTO login_logs(
                            id, user_id, login_method, ip_address, user_agent, success, created_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                userId,
                loginMethod,
                ipAddress,
                userAgent,
                success,
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
