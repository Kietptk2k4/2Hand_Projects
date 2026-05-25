package com.twohands.notification_service.integration.settings;

import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ViewNotificationSettingsApiIntegrationTest {

    private static final String SETTINGS_URL = "/api/v1/notification/notification-settings";
    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notification_settings");
    }

    @Test
    void viewNotificationSettings_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get(SETTINGS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void viewNotificationSettings_returnsDefaultsWhenUserHasNoExplicitRows() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get(SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settings.length()")
                        .value(NotificationDefaultChannelPolicy.supportedEventTypes().size()))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].allowPush").value(hasItem(true)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].allowEmail").value(hasItem(false)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].allowInApp").value(hasItem(true)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].explicitSetting").value(hasItem(false)));
    }

    @Test
    void viewNotificationSettings_returnsExplicitSettingsForCurrentUserOnly() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        insertSetting(userId, "POST_LIKED", false, false, true);
        insertSetting(otherUserId, "POST_LIKED", true, true, false);

        mockMvc.perform(get(SETTINGS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].allowPush").value(hasItem(false)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].allowEmail").value(hasItem(false)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].allowInApp").value(hasItem(true)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'POST_LIKED')].explicitSetting").value(hasItem(true)))
                .andExpect(jsonPath("$.data.settings[?(@.eventType == 'USER_FOLLOWED')].explicitSetting").value(hasItem(false)));
    }

    private void insertSetting(
            UUID userId,
            String eventType,
            boolean allowPush,
            boolean allowEmail,
            boolean allowInApp
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notification_settings(
                            user_id, event_type, allow_push, allow_email, allow_in_app, created_at, updated_at
                        )
                        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        """,
                userId,
                eventType,
                allowPush,
                allowEmail,
                allowInApp
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
