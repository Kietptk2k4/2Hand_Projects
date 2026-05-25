package com.twohands.notification_service.integration.devicetoken;

import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegisterDeviceTokenApiIntegrationTest {

    private static final String DEVICE_TOKENS_URL = "/api/v1/notification/device-tokens";
    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDeviceTokenRepository userDeviceTokenRepository;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_device_tokens");
    }

    @Test
    void registerDeviceToken_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post(DEVICE_TOKENS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceType":"ANDROID","deviceToken":"fcm-token-1"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void registerDeviceToken_returnsBadRequestForInvalidDeviceType() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceType":"DESKTOP","deviceToken":"fcm-token-1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("deviceType"));
    }

    @Test
    void registerDeviceToken_returnsBadRequestWhenTokenMissing() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceType":"ANDROID"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registerDeviceToken_upsertsActiveTokenForCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        mockMvc.perform(post(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceType":"android","deviceToken":" fcm-token-abc "}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deviceType").value("ANDROID"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.alreadyRegistered").value(false))
                .andExpect(jsonPath("$.data.id").exists());

        assertEquals(1, countTokens());
        assertTrue(userDeviceTokenRepository.existsActiveByUserId(userId));
        assertTrue(queryIsActive(userId, "fcm-token-abc"));

        mockMvc.perform(post(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceType":"ANDROID","deviceToken":"fcm-token-abc"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alreadyRegistered").value(true));

        assertEquals(1, countTokens());
    }

    @Test
    void registerDeviceToken_reassignsTokenToCurrentUser() throws Exception {
        UUID previousUserId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-20T08:00:00Z");

        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, last_used_at, created_at
                        )
                        VALUES (?, ?, 'IOS', 'shared-token', FALSE, ?, ?, ?)
                        """,
                UUID.randomUUID(),
                previousUserId,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );

        mockMvc.perform(post(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(currentUserId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deviceType":"WEB","deviceToken":"shared-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").doesNotExist())
                .andExpect(jsonPath("$.data.deviceType").value("WEB"))
                .andExpect(jsonPath("$.data.active").value(true));

        assertEquals(currentUserId, queryUserId("shared-token"));
        assertEquals("WEB", queryDeviceType("shared-token"));
        assertTrue(queryIsActive(currentUserId, "shared-token"));
        assertEquals(createdAt, queryCreatedAt("shared-token"));
    }

    private int countTokens() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_device_tokens",
                Integer.class
        );
        return count == null ? 0 : count;
    }

    private boolean queryIsActive(UUID userId, String deviceToken) {
        Boolean value = jdbcTemplate.queryForObject(
                """
                        SELECT is_active FROM user_device_tokens
                        WHERE user_id = ? AND device_token = ?
                        """,
                Boolean.class,
                userId,
                deviceToken
        );
        return Boolean.TRUE.equals(value);
    }

    private UUID queryUserId(String deviceToken) {
        return jdbcTemplate.queryForObject(
                "SELECT user_id FROM user_device_tokens WHERE device_token = ?",
                UUID.class,
                deviceToken
        );
    }

    private String queryDeviceType(String deviceToken) {
        return jdbcTemplate.queryForObject(
                "SELECT device_type FROM user_device_tokens WHERE device_token = ?",
                String.class,
                deviceToken
        );
    }

    private Instant queryCreatedAt(String deviceToken) {
        return jdbcTemplate.queryForObject(
                "SELECT created_at FROM user_device_tokens WHERE device_token = ?",
                Instant.class,
                deviceToken
        );
    }

    private String buildAccessToken(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .signWith(key)
                .compact();
    }
}
