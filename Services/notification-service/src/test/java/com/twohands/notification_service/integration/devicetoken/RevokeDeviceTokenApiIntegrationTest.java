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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RevokeDeviceTokenApiIntegrationTest {

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
    void revokeDeviceToken_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(delete(DEVICE_TOKENS_URL + "/fcm-token-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void revokeDeviceToken_returnsNotFoundWhenTokenMissing() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete(DEVICE_TOKENS_URL + "/missing-token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void revokeDeviceToken_returnsNotFoundForAnotherUsersToken() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        insertToken(ownerId, "shared-token", true);

        mockMvc.perform(delete(DEVICE_TOKENS_URL + "/shared-token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(otherUserId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void revokeDeviceToken_deactivatesOwnedTokenAndKeepsRow() throws Exception {
        UUID userId = UUID.randomUUID();
        insertToken(userId, "fcm-token-abc", true);

        mockMvc.perform(delete(DEVICE_TOKENS_URL + "/fcm-token-abc")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.alreadyRevoked").value(false))
                .andExpect(jsonPath("$.data.id").exists());

        assertEquals(1, countTokens());
        assertFalse(queryIsActive(userId, "fcm-token-abc"));
        assertFalse(userDeviceTokenRepository.existsActiveByUserId(userId));
    }

    @Test
    void revokeDeviceToken_isIdempotentWhenAlreadyInactive() throws Exception {
        UUID userId = UUID.randomUUID();
        insertToken(userId, "inactive-token", false);

        mockMvc.perform(delete(DEVICE_TOKENS_URL + "/inactive-token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.alreadyRevoked").value(true));
    }

    private void insertToken(UUID userId, String deviceToken, boolean active) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, last_used_at, created_at
                        )
                        VALUES (?, ?, 'ANDROID', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                UUID.randomUUID(),
                userId,
                deviceToken,
                active
        );
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

    private String buildAccessToken(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("USER"))
                .signWith(key)
                .compact();
    }
}
