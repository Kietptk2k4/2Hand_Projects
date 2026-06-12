package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.AdminPostModerationPayloadNormalizer;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.application.ingest.StoreNotificationEventUseCase;
import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PostModeratedNotificationIntegrationTest {

    private static final String POST_ID = "507f1f77bcf86cd799439011";

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminPostModerationPayloadNormalizer adminPostModerationPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesAuthorWithPostReferenceForHideAction() {
        UUID authorId = UUID.randomUUID();
        UUID eventId = ingestPostModeratedEvent(authorId, "HIDE", "Spam content");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, authorId));
        assertEquals("POST", queryReferenceType(eventId, authorId));
        assertEquals(POST_ID, queryReferenceId(eventId, authorId));
        assertEquals("Post hidden", queryTitle(eventId, authorId));
        assertTrue(queryMetadata(eventId, authorId).contains("moderation_reason"));
        assertFalse(queryMetadata(eventId, authorId).contains("moderated_by"));
    }

    @Test
    void process_notifiesAuthorForRemoveAction() {
        UUID authorId = UUID.randomUUID();
        UUID eventId = ingestPostModeratedEvent(authorId, "REMOVE", "Policy violation");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals("Post removed", queryTitle(eventId, authorId));
    }

    @Test
    void process_marksEventFailedWhenAuthorMissing() {
        UUID eventId = insertPostModeratedEvent(null, "{}");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    private UUID ingestPostModeratedEvent(UUID authorId, String action, String reason) {
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "POST_MODERATED",
                NotificationSourceService.ADMIN,
                "POST",
                POST_ID,
                null,
                authorId,
                """
                        {
                          "post_id":"%s",
                          "author_user_id":"%s",
                          "action":"%s",
                          "reason":"%s",
                          "moderated_by":"%s",
                          "note":"internal admin note"
                        }
                        """.formatted(POST_ID, authorId, action, reason, UUID.randomUUID())
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertPostModeratedEvent(UUID authorId, String payload) {
        UUID eventId = UUID.randomUUID();
        String storedPayload = adminPostModerationPayloadNormalizer.normalizeForStorage(
                "POST_MODERATED",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_MODERATED",
                NotificationSourceService.ADMIN,
                "POST",
                POST_ID,
                null,
                authorId,
                storedPayload,
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        ));
        return eventId;
    }

    private int countNotifications(UUID eventId, UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                Integer.class,
                eventId,
                userId
        );
        return count == null ? 0 : count;
    }

    private String queryReferenceType(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                "SELECT reference_type FROM user_notifications WHERE notification_event_id = ? AND user_id = ?",
                String.class,
                eventId,
                userId
        );
    }

    private String queryReferenceId(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                "SELECT reference_id FROM user_notifications WHERE notification_event_id = ? AND user_id = ?",
                String.class,
                eventId,
                userId
        );
    }

    private String queryTitle(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                "SELECT title FROM user_notifications WHERE notification_event_id = ? AND user_id = ?",
                String.class,
                eventId,
                userId
        );
    }

    private String queryMetadata(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                "SELECT metadata FROM user_notifications WHERE notification_event_id = ? AND user_id = ?",
                String.class,
                eventId,
                userId
        );
    }

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
