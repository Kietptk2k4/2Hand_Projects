package com.twohands.notification_service.integration.read;

import com.twohands.notification_service.application.read.CleanupOldNotificationsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "notification.workers.cleanup-old-notifications.retention-days=30",
        "notification.workers.cleanup-old-notifications.max-batches-per-run=10"
})
class CleanupOldNotificationsIntegrationTest {

    @Autowired
    private CleanupOldNotificationsUseCase cleanupOldNotificationsUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
    }

    @Test
    void execute_softDeletesOldNonCriticalNotifications() {
        UUID userId = UUID.randomUUID();
        UUID oldSocialId = UUID.randomUUID();
        UUID recentSocialId = UUID.randomUUID();
        UUID oldCriticalId = UUID.randomUUID();

        insertNotification(oldSocialId, userId, "POST_LIKED", "POST", "post-1", daysAgo(45), false);
        insertNotification(recentSocialId, userId, "POST_LIKED", "POST", "post-2", daysAgo(5), false);
        insertNotification(oldCriticalId, userId, "USER_SUSPENDED", "USER", userId.toString(), daysAgo(60), false);

        var result = cleanupOldNotificationsUseCase.execute(100);

        assertEquals(1, result.softDeletedCount());
        assertTrue(queryIsDeleted(oldSocialId));
        assertFalse(queryIsDeleted(recentSocialId));
        assertFalse(queryIsDeleted(oldCriticalId));
    }

    @Test
    void execute_skipsAlreadyDeletedAndSystemAnnouncementReference() {
        UUID userId = UUID.randomUUID();
        UUID oldAnnouncementId = UUID.randomUUID();
        UUID oldDeletedId = UUID.randomUUID();

        insertNotification(oldAnnouncementId, userId, "ORDER_CREATED", "SYSTEM_ANNOUNCEMENT", "ann-1", daysAgo(90), false);
        insertNotification(oldDeletedId, userId, "COMMENT_CREATED", "COMMENT", "c-1", daysAgo(90), true);

        var result = cleanupOldNotificationsUseCase.execute(100);

        assertEquals(0, result.softDeletedCount());
        assertFalse(queryIsDeleted(oldAnnouncementId));
        assertTrue(queryIsDeleted(oldDeletedId));
    }

    @Test
    void execute_isIdempotentOnRerun() {
        UUID userId = UUID.randomUUID();
        UUID oldId = UUID.randomUUID();
        insertNotification(oldId, userId, "USER_FOLLOWED", "USER", UUID.randomUUID().toString(), daysAgo(40), false);

        var first = cleanupOldNotificationsUseCase.execute(100);
        var second = cleanupOldNotificationsUseCase.execute(100);

        assertEquals(1, first.softDeletedCount());
        assertEquals(0, second.softDeletedCount());
        assertTrue(queryIsDeleted(oldId));
    }

    private Instant daysAgo(int days) {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }

    private void insertNotification(
            UUID id,
            UUID userId,
            String type,
            String referenceType,
            String referenceId,
            Instant createdAt,
            boolean deleted
    ) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_notifications(
                            id, notification_event_id, user_id, actor_id, type, title, content,
                            reference_type, reference_id, is_read, is_deleted, metadata,
                            delivery_status, created_at, read_at
                        )
                        VALUES (?, ?, ?, ?, ?, 'Title', 'Content', ?, ?, true, ?, '{}',
                                'SENT', ?, null)
                        """,
                id,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                type,
                referenceType,
                referenceId,
                deleted,
                Timestamp.from(createdAt)
        );
    }

    private Boolean queryIsDeleted(UUID notificationId) {
        return jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM user_notifications WHERE id = ?",
                Boolean.class,
                notificationId
        );
    }
}
