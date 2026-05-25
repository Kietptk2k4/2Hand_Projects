package com.twohands.notification_service.integration.devicetoken;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ViewUserDeviceTokensApiIntegrationTest {

    private static final String DEVICE_TOKENS_URL = "/api/v1/notification/device-tokens";
    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_device_tokens");
    }

    @Test
    void viewUserDeviceTokens_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get(DEVICE_TOKENS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void viewUserDeviceTokens_returnsEmptyListWhenUserHasNoTokens() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void viewUserDeviceTokens_returnsMaskedTokensForCurrentUserOnly() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID activeTokenId = UUID.randomUUID();
        UUID inactiveTokenId = UUID.randomUUID();

        insertToken(
                activeTokenId,
                userId,
                "ANDROID",
                "active-token-9999",
                true,
                Instant.parse("2026-05-24T12:00:00Z")
        );
        insertToken(
                inactiveTokenId,
                userId,
                "IOS",
                "inactive-token-8888",
                false,
                Instant.parse("2026-05-23T10:00:00Z")
        );
        insertToken(
                UUID.randomUUID(),
                otherUserId,
                "WEB",
                "other-user-token-7777",
                true,
                Instant.parse("2026-05-24T11:00:00Z")
        );

        mockMvc.perform(get(DEVICE_TOKENS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(activeTokenId.toString()))
                .andExpect(jsonPath("$.data.items[0].deviceType").value("ANDROID"))
                .andExpect(jsonPath("$.data.items[0].active").value(true))
                .andExpect(jsonPath("$.data.items[0].maskedDeviceToken").value("****9999"))
                .andExpect(jsonPath("$.data.items[1].id").value(inactiveTokenId.toString()))
                .andExpect(jsonPath("$.data.items[1].active").value(false))
                .andExpect(jsonPath("$.data.items[1].maskedDeviceToken").value("****8888"));
    }

    private void insertToken(
            UUID id,
            UUID userId,
            String deviceType,
            String deviceToken,
            boolean active,
            Instant updatedAt
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_device_tokens(
                            id, user_id, device_type, device_token, is_active, updated_at, last_used_at, created_at
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                userId,
                deviceType,
                deviceToken,
                active,
                Timestamp.from(updatedAt),
                Timestamp.from(updatedAt),
                Timestamp.from(updatedAt)
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
