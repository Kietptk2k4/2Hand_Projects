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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarkNotificationAsReadApiIntegrationTest {

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
    void markNotificationAsRead_returnsUnauthorizedWithoutToken() throws Exception {
        UUID notificationId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/notification/notifications/{notificationId}/read", notificationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void markNotificationAsRead_marksUnreadNotificationAsRead() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, userId, false, false, null);

        mockMvc.perform(patch("/api/v1/notification/notifications/{notificationId}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value(notificationId.toString()))
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.data.alreadyRead").value(false));

        assertEquals(Boolean.TRUE, queryIsRead(notificationId));
        assertNotNull(queryReadAt(notificationId));
    }

    @Test
    void markNotificationAsRead_isIdempotentWhenAlreadyRead() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Instant readAt = Instant.parse("2026-05-24T11:00:00Z");
        insertNotification(notificationId, userId, true, false, readAt);

        mockMvc.perform(patch("/api/v1/notification/notifications/{notificationId}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.alreadyRead").value(true))
                .andExpect(jsonPath("$.data.read").value(true));

        assertEquals(readAt, queryReadAt(notificationId));
    }

    @Test
    void markNotificationAsRead_returnsNotFoundForOtherUserNotification() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, ownerId, false, false, null);

        mockMvc.perform(patch("/api/v1/notification/notifications/{notificationId}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(otherUserId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals(Boolean.FALSE, queryIsRead(notificationId));
    }

    @Test
    void markNotificationAsRead_returnsNotFoundForDeletedNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        insertNotification(notificationId, userId, false, true, null);

        mockMvc.perform(patch("/api/v1/notification/notifications/{notificationId}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    private void insertNotification(
            UUID id,
            UUID userId,
            boolean read,
            boolean deleted,
            Instant readAt
    ) {
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
                readAt == null ? null : java.sql.Timestamp.from(readAt)
        );
    }

    private Boolean queryIsRead(UUID notificationId) {
        return jdbcTemplate.queryForObject(
                "SELECT is_read FROM user_notifications WHERE id = ?",
                Boolean.class,
                notificationId
        );
    }

    private Instant queryReadAt(UUID notificationId) {
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
