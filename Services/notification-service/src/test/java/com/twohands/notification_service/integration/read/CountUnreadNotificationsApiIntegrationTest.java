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
class CountUnreadNotificationsApiIntegrationTest {

    private static final String UNREAD_COUNT_URL = "/api/v1/notification/notifications/unread-count";
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
    void countUnreadNotifications_returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get(UNREAD_COUNT_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void countUnreadNotifications_returnsUnreadNonDeletedCountForUser() throws Exception {
        UUID userId = UUID.randomUUID();

        insertNotification(UUID.randomUUID(), userId, false, false);
        insertNotification(UUID.randomUUID(), userId, false, false);
        insertNotification(UUID.randomUUID(), userId, true, false);
        insertNotification(UUID.randomUUID(), userId, false, true);
        insertNotification(UUID.randomUUID(), UUID.randomUUID(), false, false);

        mockMvc.perform(get(UNREAD_COUNT_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + buildAccessToken(userId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(2));
    }

    @Test
    void countUnreadNotifications_matchesUnreadListTotalElements() throws Exception {
        UUID userId = UUID.randomUUID();

        insertNotification(UUID.randomUUID(), userId, false, false);
        insertNotification(UUID.randomUUID(), userId, false, false);
        insertNotification(UUID.randomUUID(), userId, true, false);

        String token = buildAccessToken(userId);

        mockMvc.perform(get(UNREAD_COUNT_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(2));

        mockMvc.perform(get(UNREAD_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meta.totalElements").value(2));
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
                read ? java.sql.Timestamp.from(Instant.now()) : null
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
