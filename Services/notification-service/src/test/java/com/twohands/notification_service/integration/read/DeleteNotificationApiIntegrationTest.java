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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeleteNotificationApiIntegrationTest {

    private static final String JWT_SECRET = "test-access-secret-key-minimum-32-characters-123456";
    private static final String NOTIFICATIONS_URL = "/api/v1/notification/notifications";
    private static final String UNREAD_COUNT_URL = "/api/v1/notification/notifications/unread-count";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
    }

    @Test
    void deleteNotification_returnsUnauthorizedWithoutToken() throws Exception {
        UUID notificationId = UUID.randomUUID();

        mockMvc.perform(delete(NOTIFICATIONS_URL + "/{notificationId}", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void deleteNotification_softDeletesOwnNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, userId, false, false);
        String token = buildAccessToken(userId);

        mockMvc.perform(delete(NOTIFICATIONS_URL + "/{notificationId}", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deleted").value(true))
                .andExpect(jsonPath("$.data.alreadyDeleted").value(false));

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
    void deleteNotification_excludesUnreadDeletedFromUnreadCount() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, userId, false, false);
        String token = buildAccessToken(userId);

        mockMvc.perform(delete(NOTIFICATIONS_URL + "/{notificationId}", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get(UNREAD_COUNT_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    void deleteNotification_returnsNotFoundForOtherUserNotification() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, ownerId, false, false);

        mockMvc.perform(delete(NOTIFICATIONS_URL + "/{notificationId}", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(otherUserId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals(Boolean.FALSE, queryIsDeleted(notificationId));
    }

    @Test
    void deleteNotification_isIdempotentWhenAlreadyDeleted() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, userId, false, true);
        String token = buildAccessToken(userId);

        mockMvc.perform(delete(NOTIFICATIONS_URL + "/{notificationId}", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alreadyDeleted").value(true));
    }

    private void insertNotification(UUID id, UUID userId, boolean read, boolean deleted) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at, read_at
                        )
                        VALUES (?, ?, ?, ?, 'POST_LIKED', 'Title', 'Content', 'POST', 'post-1',
                                ?, ?, '{}', 'SENT', CURRENT_TIMESTAMP, ?)
                        """,
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                read,
                deleted,
                read ? java.sql.Timestamp.from(java.time.Instant.now()) : null
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
