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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarkAllNotificationsAsReadApiIntegrationTest {

    private static final String READ_ALL_URL = "/api/v1/notification/notifications/read-all";
    private static final String UNREAD_COUNT_URL = "/api/v1/notification/notifications/unread-count";
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
    void markAllNotificationsAsRead_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(patch(READ_ALL_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void markAllNotificationsAsRead_marksOnlyCurrentUserUnreadNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID userUnread1 = UUID.randomUUID();
        UUID userUnread2 = UUID.randomUUID();
        UUID userRead = UUID.randomUUID();
        UUID userDeleted = UUID.randomUUID();
        UUID otherUnread = UUID.randomUUID();

        insertNotification(userUnread1, userId, false, false);
        insertNotification(userUnread2, userId, false, false);
        insertNotification(userRead, userId, true, false);
        insertNotification(userDeleted, userId, false, true);
        insertNotification(otherUnread, otherUserId, false, false);

        mockMvc.perform(patch(READ_ALL_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.updatedCount").value(2));

        assertEquals(Boolean.TRUE, queryIsRead(userUnread1));
        assertEquals(Boolean.TRUE, queryIsRead(userUnread2));
        assertNotNull(queryReadAt(userUnread1));
        assertNotNull(queryReadAt(userUnread2));
        assertEquals(Boolean.FALSE, queryIsRead(otherUnread));
    }

    @Test
    void markAllNotificationsAsRead_isIdempotentOnRepeat() throws Exception {
        UUID userId = UUID.randomUUID();
        insertNotification(UUID.randomUUID(), userId, false, false);
        String token = buildAccessToken(userId);

        mockMvc.perform(patch(READ_ALL_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(1));

        mockMvc.perform(patch(READ_ALL_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(0));

        mockMvc.perform(get(UNREAD_COUNT_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
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

    private Boolean queryIsRead(UUID notificationId) {
        return jdbcTemplate.queryForObject(
                "SELECT is_read FROM user_notifications WHERE id = ?",
                Boolean.class,
                notificationId
        );
    }

    private java.time.Instant queryReadAt(UUID notificationId) {
        java.sql.Timestamp timestamp = jdbcTemplate.queryForObject(
                "SELECT read_at FROM user_notifications WHERE id = ?",
                java.sql.Timestamp.class,
                notificationId
        );
        return timestamp == null ? null : timestamp.toInstant();
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
