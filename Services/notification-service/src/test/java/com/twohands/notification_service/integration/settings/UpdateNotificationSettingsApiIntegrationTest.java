package com.twohands.notification_service.integration.settings;

import com.twohands.notification_service.application.delivery.RespectNotificationSettingsCommand;
import com.twohands.notification_service.application.delivery.RespectNotificationSettingsUseCase;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UpdateNotificationSettingsApiIntegrationTest {

    private static final String SETTINGS_URL = "/api/v1/notification/notification-settings";
    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RespectNotificationSettingsUseCase respectNotificationSettingsUseCase;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void updateNotificationSetting_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(put(SETTINGS_URL + "/POST_LIKED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"allowPush":false,"allowEmail":false,"allowInApp":true}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void updateNotificationSetting_returnsBadRequestForUnknownEventType() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(put(SETTINGS_URL + "/UNKNOWN_EVENT")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"allowPush":false,"allowEmail":false,"allowInApp":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0].field").value("eventType"));
    }

    @Test
    void updateNotificationSetting_returnsBadRequestWhenBooleanFieldsMissing() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(put(SETTINGS_URL + "/POST_LIKED")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateNotificationSetting_upsertsSettingForCurrentUser() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = buildAccessToken(userId);

        mockMvc.perform(put(SETTINGS_URL + "/POST_LIKED")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"allowPush":false,"allowEmail":false,"allowInApp":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.eventType").value("POST_LIKED"))
                .andExpect(jsonPath("$.data.allowPush").value(false))
                .andExpect(jsonPath("$.data.allowEmail").value(false))
                .andExpect(jsonPath("$.data.allowInApp").value(true))
                .andExpect(jsonPath("$.data.explicitSetting").value(true));

        assertEquals(1, countSettingsForUser(userId));
        assertFalse(queryAllowPush(userId, "POST_LIKED"));
        assertTrue(queryAllowInApp(userId, "POST_LIKED"));

        mockMvc.perform(put(SETTINGS_URL + "/POST_LIKED")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"allowPush":true,"allowEmail":true,"allowInApp":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allowPush").value(true))
                .andExpect(jsonPath("$.data.allowInApp").value(false));

        assertEquals(1, countSettingsForUser(userId));
        assertTrue(queryAllowPush(userId, "POST_LIKED"));
        assertFalse(queryAllowInApp(userId, "POST_LIKED"));

        var deliverySettings = respectNotificationSettingsUseCase.execute(
                new RespectNotificationSettingsCommand(userId, "POST_LIKED")
        );
        assertTrue(deliverySettings.allowPush());
        assertFalse(deliverySettings.allowInApp());
        assertTrue(deliverySettings.explicitSetting());
    }

    @Test
    void updateNotificationSetting_preservesCreatedAtOnUpdate() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-20T08:00:00Z");

        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, 'POST_LIKED', TRUE, FALSE, TRUE, ?, ?)
                        """,
                userId,
                Timestamp.from(createdAt),
                Timestamp.from(createdAt)
        );

        mockMvc.perform(put(SETTINGS_URL + "/POST_LIKED")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"allowPush":false,"allowEmail":false,"allowInApp":false}
                                """))
                .andExpect(status().isOk());

        Instant persistedCreatedAt = jdbcTemplate.queryForObject(
                """
                        SELECT created_at FROM user_notification_settings
                        WHERE user_id = ? AND event_type = 'POST_LIKED'
                        """,
                Instant.class,
                userId
        );
        assertEquals(createdAt, persistedCreatedAt);
    }

    private int countSettingsForUser(UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notification_settings WHERE user_id = ?",
                Integer.class,
                userId
        );
        return count == null ? 0 : count;
    }

    private boolean queryAllowPush(UUID userId, String eventType) {
        Boolean value = jdbcTemplate.queryForObject(
                """
                        SELECT allow_push FROM user_notification_settings
                        WHERE user_id = ? AND event_type = ?
                        """,
                Boolean.class,
                userId,
                eventType
        );
        return Boolean.TRUE.equals(value);
    }

    private boolean queryAllowInApp(UUID userId, String eventType) {
        Boolean value = jdbcTemplate.queryForObject(
                """
                        SELECT allow_in_app FROM user_notification_settings
                        WHERE user_id = ? AND event_type = ?
                        """,
                Boolean.class,
                userId,
                eventType
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
