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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "auth.object-storage.enabled=true")
class AvatarUploadIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM user_settings");
        jdbcTemplate.execute("DELETE FROM user_profiles");
        jdbcTemplate.execute("DELETE FROM users");
    }

    @Test
    void shouldReturnUploadUrlForAuthenticatedUser() throws Exception {
        UUID userId = insertUser("avatar_upload@example.com");

        Map<String, Object> request = new HashMap<>();
        request.put("content_type", "image/png");
        request.put("file_size_bytes", 1_048_576);

        mockMvc.perform(post("/api/v1/users/me/avatar/upload-url")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId, "avatar_upload@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tao link upload avatar thanh cong."))
                .andExpect(jsonPath("$.data.upload_url").exists())
                .andExpect(jsonPath("$.data.object_key").value(org.hamcrest.Matchers.startsWith("avatars/" + userId)))
                .andExpect(jsonPath("$.data.avatar_url").value(org.hamcrest.Matchers.startsWith("https://minio.local/avatars/")))
                .andExpect(jsonPath("$.data.max_file_size_bytes").value(5_242_880))
                .andExpect(jsonPath("$.data.allowed_content_types[0]").value("image/jpeg"));
    }

    @Test
    void shouldReturn400ForInvalidContentType() throws Exception {
        UUID userId = insertUser("avatar_bad_type@example.com");

        Map<String, Object> request = Map.of(
                "content_type", "application/pdf",
                "file_size_bytes", 1024
        );

        mockMvc.perform(post("/api/v1/users/me/avatar/upload-url")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId, "avatar_bad_type@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturn400WhenFileTooLarge() throws Exception {
        UUID userId = insertUser("avatar_too_large@example.com");

        Map<String, Object> request = Map.of(
                "content_type", "image/jpeg",
                "file_size_bytes", 6_000_000
        );

        mockMvc.perform(post("/api/v1/users/me/avatar/upload-url")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId, "avatar_too_large@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("file_size_bytes"));
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        Map<String, Object> request = Map.of(
                "content_type", "image/jpeg",
                "file_size_bytes", 1024
        );

        mockMvc.perform(post("/api/v1/users/me/avatar/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
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
                        VALUES (?, 'Test User', NULL, NULL, NULL, NULL, false, ?, ?)
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

    private String bearerTokenFor(UUID userId, String email) {
        return "Bearer " + jwtTokenIssuer.issue(userId, email, "ACTIVE", Instant.now()).accessToken();
    }
}
