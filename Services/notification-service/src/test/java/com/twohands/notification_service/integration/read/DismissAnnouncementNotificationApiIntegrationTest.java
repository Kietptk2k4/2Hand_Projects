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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DismissAnnouncementNotificationApiIntegrationTest {

    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";
    private static final String NOTIFICATIONS_URL = "/api/v1/notification/notifications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
    }

    @Test
    void dismissAnnouncement_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post(NOTIFICATIONS_URL + "/{notificationId}/dismiss", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void dismissAnnouncement_softDeletesDismissibleAnnouncement() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertAnnouncementNotification(notificationId, userId, true, false);
        String token = buildAccessToken(userId);

        mockMvc.perform(post(NOTIFICATIONS_URL + "/{notificationId}/dismiss", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dismissed").value(true))
                .andExpect(jsonPath("$.data.alreadyDismissed").value(false));

        assertEquals(Boolean.TRUE, queryIsDeleted(notificationId));

        mockMvc.perform(get(NOTIFICATIONS_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void dismissAnnouncement_returnsConflictWhenNotDismissible() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertAnnouncementNotification(notificationId, userId, false, false);
        String token = buildAccessToken(userId);

        mockMvc.perform(post(NOTIFICATIONS_URL + "/{notificationId}/dismiss", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(409));

        assertEquals(Boolean.FALSE, queryIsDeleted(notificationId));
    }

    @Test
    void dismissAnnouncement_returnsBadRequestForNonAnnouncement() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertPostNotification(notificationId, userId);
        String token = buildAccessToken(userId);

        mockMvc.perform(post(NOTIFICATIONS_URL + "/{notificationId}/dismiss", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void dismissAnnouncement_returnsNotFoundForOtherUser() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertAnnouncementNotification(notificationId, ownerId, true, false);

        mockMvc.perform(post(NOTIFICATIONS_URL + "/{notificationId}/dismiss", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(otherUserId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void dismissAnnouncement_isIdempotentWhenAlreadyDismissed() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertAnnouncementNotification(notificationId, userId, true, true);
        String token = buildAccessToken(userId);

        mockMvc.perform(post(NOTIFICATIONS_URL + "/{notificationId}/dismiss", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alreadyDismissed").value(true));
    }

    private void insertAnnouncementNotification(
            UUID id,
            UUID userId,
            boolean dismissible,
            boolean deleted
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at, read_at
                        )
                        VALUES (?, ?, ?, ?, 'SYSTEM_ANNOUNCEMENT_SENT', 'Announcement', 'Body',
                                'SYSTEM_ANNOUNCEMENT', 'ann-1', false, ?, ?,
                                'SENT', CURRENT_TIMESTAMP, null)
                        """,
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                deleted,
                "{\"dismissible\":" + dismissible + ",\"severity\":\"INFO\",\"is_pinned\":false}"
        );
    }

    private void insertPostNotification(UUID id, UUID userId) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at, read_at
                        )
                        VALUES (?, ?, ?, ?, 'POST_LIKED', 'Title', 'Content',
                                'POST', 'post-1', false, false, '{}',
                                'SENT', CURRENT_TIMESTAMP, null)
                        """,
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID()
        );
    }

    private Boolean queryIsDeleted(UUID notificationId) {
        return jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM user_notifications WHERE id = ?",
                Boolean.class,
                notificationId
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
