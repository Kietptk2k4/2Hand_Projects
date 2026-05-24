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
class ViewUnreadNotificationsApiIntegrationTest {

    private static final String UNREAD_URL = "/api/v1/notification/notifications/unread";
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
    void viewUnreadNotifications_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get(UNREAD_URL)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void viewUnreadNotifications_returnsOnlyUnreadNonDeletedNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID unreadId = UUID.randomUUID();

        insertNotification(unreadId, userId, "Unread notification", false, false, Instant.parse("2026-05-24T12:00:00Z"));
        insertNotification(UUID.randomUUID(), userId, "Read notification", true, false, Instant.parse("2026-05-24T11:00:00Z"));
        insertNotification(UUID.randomUUID(), userId, "Deleted unread", false, true, Instant.parse("2026-05-24T10:00:00Z"));
        insertNotification(UUID.randomUUID(), UUID.randomUUID(), "Other user unread", false, false, Instant.now());

        mockMvc.perform(get(UNREAD_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(unreadId.toString()))
                .andExpect(jsonPath("$.data.items[0].read").value(false))
                .andExpect(jsonPath("$.data.meta.totalElements").value(1));
    }

    @Test
    void viewUnreadNotifications_returnsBadRequestForInvalidPagination() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get(UNREAD_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .param("page", "0")
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private void insertNotification(
            UUID id,
            UUID userId,
            String title,
            boolean read,
            boolean deleted,
            Instant createdAt
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at, read_at
                        )
                        VALUES (?, ?, ?, ?, 'POST_LIKED', ?, 'Content', 'POST', 'post-1',
                                ?, ?, '{}', 'SENT', ?, ?)
                        """,
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                title,
                read,
                deleted,
                java.sql.Timestamp.from(createdAt),
                read ? java.sql.Timestamp.from(createdAt) : null
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
