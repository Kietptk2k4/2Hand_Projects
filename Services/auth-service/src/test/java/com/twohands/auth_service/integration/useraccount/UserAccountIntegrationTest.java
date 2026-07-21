package com.twohands.auth_service.integration.useraccount;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAccountIntegrationTest {

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
        ensureRbacTables();
        jdbcTemplate.execute("DELETE FROM login_logs");
        jdbcTemplate.execute("DELETE FROM refresh_token_sessions");
        jdbcTemplate.execute("DELETE FROM outbox_events");
        jdbcTemplate.execute("DELETE FROM verification_tokens");
        jdbcTemplate.execute("DELETE FROM user_roles");
        jdbcTemplate.execute("DELETE FROM role_permissions");
        jdbcTemplate.execute("DELETE FROM permissions");
        jdbcTemplate.execute("DELETE FROM roles");
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
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
                    description TEXT,
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

    @Test
    void viewAccountShouldReturnCombinedData() throws Exception {
        TestUser user = insertFullUser("view_account@example.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.id").value(user.userId().toString()))
                .andExpect(jsonPath("$.data.profile.display_name").value("Initial Name"))
                .andExpect(jsonPath("$.data.settings.appearance_mode").value("SYSTEM"));
    }

    @Test
    void viewSessionsShouldReturnOnlyActiveSessionsSortedDesc() throws Exception {
        TestUser user = insertFullUser("view_sessions@example.com");

        Instant older = Instant.now().minusSeconds(300);
        Instant newer = Instant.now().minusSeconds(120);
        insertSession(user.userId(), "device-old", "10.0.0.1", "ACTIVE", older);
        insertSession(user.userId(), "device-new", "10.0.0.2", "ACTIVE", newer);
        insertSession(user.userId(), "device-revoked", "10.0.0.3", "REVOKED", Instant.now().minusSeconds(30));

        mockMvc.perform(get("/api/v1/users/me/sessions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay danh sach phien dang nhap thanh cong."))
                .andExpect(jsonPath("$.data.sessions.length()").value(2))
                .andExpect(jsonPath("$.data.sessions[0].device_id").value("device-new"))
                .andExpect(jsonPath("$.data.sessions[1].device_id").value("device-old"))
                .andExpect(jsonPath("$.data.sessions[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.sessions[0].token_hash").doesNotExist());
    }

    @Test
    void viewSessionsShouldReturnEmptyListWhenNoActiveSession() throws Exception {
        TestUser user = insertFullUser("view_sessions_empty@example.com");
        insertSession(user.userId(), "device-revoked", "10.10.10.1", "REVOKED", Instant.now().minusSeconds(100));

        mockMvc.perform(get("/api/v1/users/me/sessions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessions.length()").value(0));
    }

    @Test
    void logoutAllSessionsShouldRevokeActiveSessions() throws Exception {
        TestUser user = insertFullUser("logout_all_active@example.com");
        insertSession(user.userId(), "device-1", "10.0.1.1", "ACTIVE", Instant.now().minusSeconds(300));
        insertSession(user.userId(), "device-2", "10.0.1.2", "ACTIVE", Instant.now().minusSeconds(200));
        insertSession(user.userId(), "device-3", "10.0.1.3", "REVOKED", Instant.now().minusSeconds(100));

        mockMvc.perform(post("/api/v1/users/me/sessions/logout-all")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dang xuat tat ca phien dang nhap thanh cong."));

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

        assertEquals(0, activeSessions);
        assertEquals(3, revokedSessions);
    }

    @Test
    void logoutAllSessionsWithoutActiveShouldStillReturn200() throws Exception {
        TestUser user = insertFullUser("logout_all_no_active@example.com");
        insertSession(user.userId(), "device-1", "10.0.2.1", "REVOKED", Instant.now().minusSeconds(300));
        insertSession(user.userId(), "device-2", "10.0.2.2", "LOGGED_OUT", Instant.now().minusSeconds(200));

        mockMvc.perform(post("/api/v1/users/me/sessions/logout-all")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dang xuat tat ca phien dang nhap thanh cong."));

        Integer activeSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                user.userId()
        );
        assertEquals(0, activeSessions);
    }

    @Test
    void updateProfileShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("update_profile@example.com");

        Map<String, Object> request = new HashMap<>();
        request.put("display_name", "Kiet Tran");
        request.put("bio", "Backend engineer");
        request.put("website", "https://example.com");
        request.put("social_links", Map.of("github", "https://github.com/kiet"));

        mockMvc.perform(put("/api/v1/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String displayName = jdbcTemplate.queryForObject(
                "SELECT display_name FROM user_profiles WHERE user_id = ?",
                String.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals("Kiet Tran", displayName);
        assertEquals(1, outboxCount);
    }

    @Test
    void updateAvatarShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("update_avatar@example.com");

        Map<String, Object> request = Map.of("avatar_url", "https://minio.local/2hands-avatar/u1.png");

        mockMvc.perform(patch("/api/v1/users/me/avatar")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String avatarUrl = jdbcTemplate.queryForObject(
                "SELECT avatar_url FROM user_profiles WHERE user_id = ?",
                String.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals("https://minio.local/2hands-avatar/u1.png", avatarUrl);
        assertEquals(1, outboxCount);
    }

    @Test
    void updateCoverShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("update_cover@example.com");

        Map<String, Object> request = Map.of("cover_url", "https://minio.local/2hands-cover/u1.png");

        mockMvc.perform(patch("/api/v1/users/me/cover")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String coverUrl = jdbcTemplate.queryForObject(
                "SELECT cover_url FROM user_profiles WHERE user_id = ?",
                String.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals("https://minio.local/2hands-cover/u1.png", coverUrl);
        assertEquals(1, outboxCount);
    }

    @Test
    void togglePrivacyShouldPersistAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("toggle_privacy@example.com");

        Map<String, Object> request = Map.of("is_private", true);

        mockMvc.perform(patch("/api/v1/users/me/privacy")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Boolean isPrivate = jdbcTemplate.queryForObject(
                "SELECT is_private FROM user_profiles WHERE user_id = ?",
                Boolean.class,
                user.userId()
        );
        Integer outboxCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_UPDATED' AND status = 'PENDING'",
                Integer.class
        );
        assertEquals(true, isPrivate);
        assertEquals(1, outboxCount);
    }

    @Test
    void updateSettingsShouldPersistAppearanceMode() throws Exception {
        TestUser user = insertFullUser("update_settings@example.com");

        Map<String, Object> request = Map.of("appearance_mode", "DARK");

        mockMvc.perform(patch("/api/v1/users/me/settings")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.appearance_mode").value("DARK"));

        String appearanceMode = jdbcTemplate.queryForObject(
                "SELECT appearance_mode FROM user_settings WHERE user_id = ?",
                String.class,
                user.userId()
        );
        assertEquals("DARK", appearanceMode);
    }

    @Test
    void softDeleteShouldSetDeletedRevokeSessionsAndWriteOutbox() throws Exception {
        TestUser user = insertFullUser("soft_delete@example.com");
        insertActiveSession(user.userId());
        insertActiveSession(user.userId());

        Map<String, Object> request = Map.of("password", "Password123");

        mockMvc.perform(post("/api/v1/users/me/soft-delete")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String statusValue = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, user.userId());
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
                "SELECT COUNT(*) FROM outbox_events WHERE event_type = 'USER_DELETED' AND status = 'PENDING'",
                Integer.class
        );

        assertEquals("DELETED", statusValue);
        assertEquals(1, deletedAtCount);
        assertEquals(0, activeSessions);
        assertEquals(2, revokedSessions);
        assertEquals(1, outboxCount);
    }

    @Test
    void softDeleteWrongPasswordShouldReturn400AndNoChanges() throws Exception {
        TestUser user = insertFullUser("soft_delete_wrong_password@example.com");
        insertActiveSession(user.userId());

        String beforeStatus = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, user.userId());
        Integer beforeOutbox = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);

        mockMvc.perform(post("/api/v1/users/me/soft-delete")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "WrongPassword123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mat khau khong chinh xac."));

        String afterStatus = jdbcTemplate.queryForObject("SELECT status FROM users WHERE id = ?", String.class, user.userId());
        Integer afterActiveSessions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_token_sessions WHERE user_id = ? AND status = 'ACTIVE'",
                Integer.class,
                user.userId()
        );
        Integer afterOutbox = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM outbox_events", Integer.class);
        Integer deletedAtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND deleted_at IS NOT NULL",
                Integer.class,
                user.userId()
        );

        assertEquals(beforeStatus, afterStatus);
        assertEquals(1, afterActiveSessions);
        assertEquals(beforeOutbox, afterOutbox);
        assertEquals(0, deletedAtCount);
    }

    @Test
    void missingJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void logoutAllSessionsMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/users/me/sessions/logout-all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void trackLoginHistoryShouldReturnItemsWithLimitOffsetAndBothSuccessStates() throws Exception {
        TestUser user = insertFullUser("track_history_items@example.com");
        insertLoginLog(user.userId(), "EMAIL", "1.1.1.1", "Agent A", true, Instant.now().minusSeconds(30));
        insertLoginLog(user.userId(), "GOOGLE", "1.1.1.2", "Agent B", false, Instant.now().minusSeconds(20));
        insertLoginLog(user.userId(), "FACEBOOK", "1.1.1.3", "Agent C", true, Instant.now().minusSeconds(10));

        mockMvc.perform(get("/api/v1/users/me/login-history")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay lich su dang nhap thanh cong."))
                .andExpect(jsonPath("$.data.limit").value(2))
                .andExpect(jsonPath("$.data.offset").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].login_method").value("FACEBOOK"))
                .andExpect(jsonPath("$.data.items[1].login_method").value("GOOGLE"))
                .andExpect(jsonPath("$.data.items[0].success").value(true))
                .andExpect(jsonPath("$.data.items[1].success").value(false));
    }

    @Test
    void trackLoginHistoryShouldReturnEmptyList() throws Exception {
        TestUser user = insertFullUser("track_history_empty@example.com");

        mockMvc.perform(get("/api/v1/users/me/login-history")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(0))
                .andExpect(jsonPath("$.data.limit").value(20))
                .andExpect(jsonPath("$.data.offset").value(0));
    }

    @Test
    void trackLoginHistoryInvalidPaginationShouldReturn400() throws Exception {
        TestUser user = insertFullUser("track_history_invalid_pagination@example.com");

        mockMvc.perform(get("/api/v1/users/me/login-history")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("limit"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_RANGE"));

        mockMvc.perform(get("/api/v1/users/me/login-history")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(user.userId(), user.email()))
                        .param("offset", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("offset"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_RANGE"));
    }

    @Test
    void trackLoginHistoryMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/login-history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void assignRoleShouldReturn200WhenSuccess() throws Exception {
        TestUser actor = insertFullUser("assign_role_actor_success@example.com");
        TestUser target = insertFullUser("assign_role_target_success@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        String body = objectMapper.writeValueAsString(Map.of("role_id", roleId.toString()));

        mockMvc.perform(post("/api/v1/admin/users/" + target.userId() + "/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Gan role cho user thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(target.userId().toString()))
                .andExpect(jsonPath("$.data.role_id").value(roleId.toString()));
    }

    @Test
    void assignRoleShouldReturn409WhenDuplicateAssignment() throws Exception {
        TestUser actor = insertFullUser("assign_role_actor_duplicate@example.com");
        TestUser target = insertFullUser("assign_role_target_duplicate@example.com");
        UUID roleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);
        insertUserRole(target.userId(), roleId);

        String body = objectMapper.writeValueAsString(Map.of("role_id", roleId.toString()));

        mockMvc.perform(post("/api/v1/admin/users/" + target.userId() + "/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("role_id"))
                .andExpect(jsonPath("$.errors[0].reason").value("ALREADY_ASSIGNED"));
    }

    @Test
    void assignRoleShouldReturn403WhenActorLacksPermission() throws Exception {
        TestUser actor = insertFullUser("assign_role_actor_forbidden@example.com");
        TestUser target = insertFullUser("assign_role_target_forbidden@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");

        String body = objectMapper.writeValueAsString(Map.of("role_id", roleId.toString()));

        mockMvc.perform(post("/api/v1/admin/users/" + target.userId() + "/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void assignRoleShouldReturn404WhenUserOrRoleNotFound() throws Exception {
        TestUser actor = insertFullUser("assign_role_actor_not_found@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        String bodyWithMissingRole = objectMapper.writeValueAsString(Map.of("role_id", UUID.randomUUID().toString()));

        mockMvc.perform(post("/api/v1/admin/users/" + UUID.randomUUID() + "/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithMissingRole))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void assignRoleShouldReturn403WhenSelfAssign() throws Exception {
        TestUser actor = insertFullUser("assign_role_actor_self@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        String body = objectMapper.writeValueAsString(Map.of("role_id", roleId.toString()));

        mockMvc.perform(post("/api/v1/admin/users/" + actor.userId() + "/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void assignRoleShouldReturn400WhenInvalidUuidInput() throws Exception {
        TestUser actor = insertFullUser("assign_role_actor_invalid_uuid@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        mockMvc.perform(post("/api/v1/admin/users/not-a-uuid/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role_id", roleId.toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("userId"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));

        mockMvc.perform(post("/api/v1/admin/users/" + UUID.randomUUID() + "/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role_id", "invalid-role-id"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("role_id"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));
    }

    @Test
    void assignRoleMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users/" + UUID.randomUUID() + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role_id", UUID.randomUUID().toString()))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void revokeRoleShouldReturn200WhenSuccess() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_success@example.com");
        TestUser target = insertFullUser("revoke_role_target_success@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);
        insertUserRole(target.userId(), roleId);

        mockMvc.perform(delete("/api/v1/admin/users/" + target.userId() + "/roles/" + roleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thu hoi role khoi user thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(target.userId().toString()))
                .andExpect(jsonPath("$.data.role_id").value(roleId.toString()));
    }

    @Test
    void revokeRoleShouldReturn409WhenRoleNotAssigned() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_conflict@example.com");
        TestUser target = insertFullUser("revoke_role_target_conflict@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        mockMvc.perform(delete("/api/v1/admin/users/" + target.userId() + "/roles/" + roleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("role_id"))
                .andExpect(jsonPath("$.errors[0].reason").value("ROLE_NOT_ASSIGNED"));
    }

    @Test
    void revokeRoleShouldReturn403WhenActorLacksPermission() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_forbidden@example.com");
        TestUser target = insertFullUser("revoke_role_target_forbidden@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        insertUserRole(target.userId(), roleId);

        mockMvc.perform(delete("/api/v1/admin/users/" + target.userId() + "/roles/" + roleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void revokeRoleShouldReturn404WhenUserOrRoleNotFound() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_not_found@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        mockMvc.perform(delete("/api/v1/admin/users/" + UUID.randomUUID() + "/roles/" + UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void revokeRoleShouldReturn403WhenSelfRevoke() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_self@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        mockMvc.perform(delete("/api/v1/admin/users/" + actor.userId() + "/roles/" + roleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void revokeRoleShouldReturn403ForLastSuperAdminProtection() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_last_admin@example.com");
        TestUser target = insertFullUser("revoke_role_target_last_admin@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);
        insertUserRole(target.userId(), adminRoleId);
        // Make target become the last ADMIN holder by removing actor ADMIN role.
        removeUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(delete("/api/v1/admin/users/" + target.userId() + "/roles/" + adminRoleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void revokeRoleShouldReturn400WhenInvalidUuidInput() throws Exception {
        TestUser actor = insertFullUser("revoke_role_actor_invalid_uuid@example.com");
        UUID roleId = insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(roleId, assignRolePermissionId);
        insertUserRole(actor.userId(), roleId);

        mockMvc.perform(delete("/api/v1/admin/users/not-a-uuid/roles/" + roleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("userId"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));

        mockMvc.perform(delete("/api/v1/admin/users/" + UUID.randomUUID() + "/roles/not-a-role-id")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("roleId"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));
    }

    @Test
    void revokeRoleMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/" + UUID.randomUUID() + "/roles/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void viewRoleListShouldReturn200WithRoles() throws Exception {
        TestUser actor = insertFullUser("view_role_list_actor_success@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        insertRole("MODERATOR", "Moderator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay danh sach role thanh cong."))
                .andExpect(jsonPath("$.data.roles.length()").value(2))
                .andExpect(jsonPath("$.data.roles[0].code").value("ADMIN"))
                .andExpect(jsonPath("$.data.roles[1].code").value("MODERATOR"));
    }

    @Test
    void viewRoleListShouldReturn403WhenActorLacksPermission() throws Exception {
        TestUser actor = insertFullUser("view_role_list_actor_forbidden@example.com");

        mockMvc.perform(get("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void viewRoleListMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void createRoleShouldReturn201WithCreatedRole() throws Exception {
        TestUser actor = insertFullUser("create_role_actor_success@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("code", "support");
        payload.put("name", "Support team");

        mockMvc.perform(post("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tao vai tro thanh cong."))
                .andExpect(jsonPath("$.data.code").value("SUPPORT"))
                .andExpect(jsonPath("$.data.name").value("Support team"));
    }

    @Test
    void createRoleShouldReturn403WhenActorLacksPermission() throws Exception {
        TestUser actor = insertFullUser("create_role_actor_forbidden@example.com");

        Map<String, Object> payload = new HashMap<>();
        payload.put("code", "SUPPORT");
        payload.put("name", "Support team");

        mockMvc.perform(post("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void updateRoleShouldReturn200WithUpdatedName() throws Exception {
        TestUser actor = insertFullUser("update_role_actor_success@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID supportRoleId = insertRole("SUPPORT", "Support");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Support team");

        mockMvc.perform(patch("/api/v1/admin/roles/" + supportRoleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cap nhat vai tro thanh cong."))
                .andExpect(jsonPath("$.data.id").value(supportRoleId.toString()))
                .andExpect(jsonPath("$.data.code").value("SUPPORT"))
                .andExpect(jsonPath("$.data.name").value("Support team"));
    }

    @Test
    void updateRoleShouldReturn403ForSystemRole() throws Exception {
        TestUser actor = insertFullUser("update_role_actor_system@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Super admin");

        mockMvc.perform(patch("/api/v1/admin/roles/" + adminRoleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Khong the sua vai tro he thong."));
    }

    @Test
    void deleteRoleShouldReturn200WhenRoleIsUnused() throws Exception {
        TestUser actor = insertFullUser("delete_role_actor_success@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID supportRoleId = insertRole("SUPPORT", "Support");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(delete("/api/v1/admin/roles/" + supportRoleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xoa vai tro thanh cong."))
                .andExpect(jsonPath("$.data.id").value(supportRoleId.toString()))
                .andExpect(jsonPath("$.data.code").value("SUPPORT"));
    }

    @Test
    void deleteRoleShouldReturn403ForSystemRole() throws Exception {
        TestUser actor = insertFullUser("delete_role_actor_system@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(delete("/api/v1/admin/roles/" + adminRoleId)
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Khong the xoa vai tro he thong."));
    }

    @Test
    void viewPermissionsOfRoleShouldReturn200WithPermissions() throws Exception {
        TestUser actor = insertFullUser("view_role_permissions_actor_success@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        UUID userReadPermissionId = insertPermission("USER_READ", "Read user information");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertRolePermission(adminRoleId, userReadPermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/roles/" + adminRoleId + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay danh sach permission cua role thanh cong."))
                .andExpect(jsonPath("$.data.role.id").value(adminRoleId.toString()))
                .andExpect(jsonPath("$.data.role.code").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions.length()").value(2));
    }

    @Test
    void viewPermissionsOfRoleShouldReturn200WithEmptyPermissions() throws Exception {
        TestUser actor = insertFullUser("view_role_permissions_actor_empty@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        UUID emptyRoleId = insertRole("MODERATOR", "Moderator");

        mockMvc.perform(get("/api/v1/admin/roles/" + emptyRoleId + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role.id").value(emptyRoleId.toString()))
                .andExpect(jsonPath("$.data.permissions.length()").value(0));
    }

    @Test
    void viewPermissionsOfRoleShouldReturn404WhenRoleMissing() throws Exception {
        TestUser actor = insertFullUser("view_role_permissions_actor_not_found@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/roles/" + UUID.randomUUID() + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void viewPermissionsOfRoleShouldReturn403WhenActorLacksPermission() throws Exception {
        TestUser actor = insertFullUser("view_role_permissions_actor_forbidden@example.com");
        UUID roleId = insertRole("ADMIN", "Administrator");

        mockMvc.perform(get("/api/v1/admin/roles/" + roleId + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void viewPermissionsOfRoleShouldReturn400WhenRoleIdInvalidFormat() throws Exception {
        TestUser actor = insertFullUser("view_role_permissions_actor_invalid_role_id@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/roles/not-a-role-id/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("roleId"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));
    }

    @Test
    void viewPermissionsOfRoleMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/roles/" + UUID.randomUUID() + "/permissions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void checkUserPermissionShouldReturn200WithPermissions() throws Exception {
        TestUser actor = insertFullUser("check_user_permission_actor_success@example.com");
        TestUser target = insertFullUser("check_user_permission_target_success@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        UUID userUpdatePermissionId = insertPermission("USER_UPDATE", "Update user information");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertRolePermission(adminRoleId, userUpdatePermissionId);
        insertUserRole(actor.userId(), adminRoleId);
        insertUserRole(target.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/users/" + target.userId() + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lay danh sach permission cua user thanh cong."))
                .andExpect(jsonPath("$.data.user_id").value(target.userId().toString()))
                .andExpect(jsonPath("$.data.permissions.length()").value(2))
                .andExpect(jsonPath("$.data.permissions[0].code").value("ASSIGN_ROLE"))
                .andExpect(jsonPath("$.data.permissions[1].code").value("USER_UPDATE"));
    }

    @Test
    void checkUserPermissionShouldReturn200WithEmptyPermissions() throws Exception {
        TestUser actor = insertFullUser("check_user_permission_actor_empty@example.com");
        TestUser target = insertFullUser("check_user_permission_target_empty@example.com");
        UUID actorRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(actorRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), actorRoleId);

        mockMvc.perform(get("/api/v1/admin/users/" + target.userId() + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_id").value(target.userId().toString()))
                .andExpect(jsonPath("$.data.permissions.length()").value(0));
    }

    @Test
    void checkUserPermissionShouldReturn403WhenActorLacksPermission() throws Exception {
        TestUser actor = insertFullUser("check_user_permission_actor_forbidden@example.com");
        TestUser target = insertFullUser("check_user_permission_target_forbidden@example.com");

        mockMvc.perform(get("/api/v1/admin/users/" + target.userId() + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void checkUserPermissionShouldReturn404WhenTargetUserMissingOrDeleted() throws Exception {
        TestUser actor = insertFullUser("check_user_permission_actor_not_found@example.com");
        TestUser targetDeleted = insertFullUser("check_user_permission_target_deleted@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/users/" + UUID.randomUUID() + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        jdbcTemplate.update("UPDATE users SET status = 'DELETED', deleted_at = ? WHERE id = ?",
                Timestamp.from(Instant.now()), targetDeleted.userId());

        mockMvc.perform(get("/api/v1/admin/users/" + targetDeleted.userId() + "/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void checkUserPermissionShouldReturn400WhenUserIdInvalidFormat() throws Exception {
        TestUser actor = insertFullUser("check_user_permission_actor_invalid_user_id@example.com");
        UUID adminRoleId = insertRole("ADMIN", "Administrator");
        UUID assignRolePermissionId = insertPermission("ASSIGN_ROLE", "Assign role permission");
        insertRolePermission(adminRoleId, assignRolePermissionId);
        insertUserRole(actor.userId(), adminRoleId);

        mockMvc.perform(get("/api/v1/admin/users/not-a-uuid/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(actor.userId(), actor.email())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("userId"))
                .andExpect(jsonPath("$.errors[0].reason").value("INVALID_FORMAT"));
    }

    @Test
    void checkUserPermissionMissingJwtShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/" + UUID.randomUUID() + "/permissions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
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

    private void insertSession(UUID userId, String deviceId, String ip, String status, Instant createdAt) {
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
                ip,
                "JUnit",
                Timestamp.from(createdAt.plusSeconds(3600)),
                status,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );
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

    private void removeUserRole(UUID userId, UUID roleId) {
        jdbcTemplate.update(
                "DELETE FROM user_roles WHERE user_id = ? AND role_id = ?",
                userId,
                roleId
        );
    }

    private String bearerTokenFor(UUID userId, String email) {
        return "Bearer " + jwtTokenIssuer.issue(userId, email, "ACTIVE", Instant.now()).accessToken();
    }

    private record TestUser(UUID userId, String email) {
    }
}
