package com.twohands.notification_service.integration.read;

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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ViewUserNotificationsApiIntegrationTest {

    private static final String NOTIFICATIONS_URL = "/api/v1/notification/notifications";
    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
    }

    @Test
    void viewUserNotifications_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get(NOTIFICATIONS_URL)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void viewUserNotifications_returnsPaginatedNotificationsForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID visibleId = UUID.randomUUID();
        UUID deletedId = UUID.randomUUID();

        insertNotification(visibleId, userId, "Newer notification", false, Instant.parse("2026-05-24T12:00:00Z"));
        insertNotification(UUID.randomUUID(), userId, "Older notification", false, Instant.parse("2026-05-24T10:00:00Z"));
        insertNotification(deletedId, userId, "Deleted notification", true, Instant.parse("2026-05-24T11:00:00Z"));
        insertNotification(UUID.randomUUID(), otherUserId, "Other user notification", false, Instant.now());

        mockMvc.perform(get(NOTIFICATIONS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(visibleId.toString()))
                .andExpect(jsonPath("$.data.items[0].title").value("Newer notification"))
                .andExpect(jsonPath("$.data.meta.totalElements").value(2))
                .andExpect(jsonPath("$.data.meta.page").value(0))
                .andExpect(jsonPath("$.data.meta.size").value(10));
    }

    @Test
    void viewUserNotifications_returnsBadRequestForInvalidPagination() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get(NOTIFICATIONS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .param("page", "-1")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void viewUserNotifications_redactsSensitiveMetadataInResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at
                        )
                        VALUES (?, ?, ?, ?, 'POST_LIKED', 'New like', 'Someone liked your post.',
                                'POST', 'post-1', FALSE, FALSE, ?, 'SENT', CURRENT_TIMESTAMP)
                        """,
                notificationId,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                "{\"actorName\":\"Alice\",\"access_token\":\"secret-token\"}"
        );

        mockMvc.perform(get(NOTIFICATIONS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].metadata").value(org.hamcrest.Matchers.containsString("***REDACTED***")))
                .andExpect(jsonPath("$.data.items[0].metadata").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret-token"))));
    }

    private void insertNotification(
            UUID id,
            UUID userId,
            String title,
            boolean deleted,
            Instant createdAt
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at
                        )
                        VALUES (?, ?, ?, ?, 'POST_LIKED', ?, 'Content', 'POST', 'post-1',
                                FALSE, ?, '{}', 'SENT', ?)
                        """,
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                title,
                deleted,
                java.sql.Timestamp.from(createdAt)
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
