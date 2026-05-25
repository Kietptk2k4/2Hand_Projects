package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.AdminReviewModerationPayloadNormalizer;
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
class ReviewHiddenNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AdminReviewModerationPayloadNormalizer adminReviewModerationPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesAuthorWithReviewReference() {
        UUID authorId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID eventId = ingestReviewHiddenEvent(authorId, null, reviewId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, authorId));
        assertEquals("REVIEW", queryReferenceType(eventId, authorId));
        assertEquals(reviewId.toString(), queryReferenceId(eventId, authorId));
        assertEquals("Review hidden", queryTitle(eventId, authorId));
        assertTrue(queryMetadata(eventId, authorId).contains("hidden_reason"));
        assertFalse(queryMetadata(eventId, authorId).contains("hidden_by"));
    }

    @Test
    void process_notifiesAuthorAndSellerWhenBothPresent() {
        UUID authorId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID eventId = ingestReviewHiddenEvent(authorId, sellerId, reviewId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, authorId));
        assertEquals(1, countNotifications(eventId, sellerId));
        assertEquals("Review hidden on your product", queryTitle(eventId, sellerId));
    }

    @Test
    void process_marksEventFailedWhenNoRecipient() {
        UUID reviewId = UUID.randomUUID();
        UUID eventId = insertReviewHiddenEvent(null, null, reviewId, "{}");

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID authorId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID eventId = ingestReviewHiddenEvent(authorId, null, reviewId);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, authorId));
    }

    private UUID ingestReviewHiddenEvent(UUID authorId, UUID sellerId, UUID reviewId) {
        String sellerField = sellerId == null ? "" : ",\"seller_user_id\":\"%s\"".formatted(sellerId);
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                null,
                "REVIEW_HIDDEN",
                NotificationSourceService.ADMIN,
                "REVIEW",
                reviewId.toString(),
                null,
                authorId,
                """
                        {
                          "review_id":"%s",
                          "review_author_id":"%s"%s,
                          "reason":"Inappropriate content",
                          "hidden_by":"%s",
                          "note":"internal admin note"
                        }
                        """.formatted(reviewId, authorId, sellerField, UUID.randomUUID())
        ));
        return ingestResult.notificationEventId();
    }

    private UUID insertReviewHiddenEvent(UUID authorId, UUID sellerId, UUID reviewId, String payload) {
        UUID eventId = UUID.randomUUID();
        String storedPayload = adminReviewModerationPayloadNormalizer.normalizeForStorage(
                "REVIEW_HIDDEN",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "REVIEW_HIDDEN",
                NotificationSourceService.ADMIN,
                "REVIEW",
                reviewId.toString(),
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

    private NotificationEvent reopenForReprocess(UUID eventId) {
        NotificationEvent existing = notificationEventRepository.findById(eventId).orElseThrow();
        return new NotificationEvent(
                existing.id(),
                existing.sourceEventId(),
                existing.eventKey(),
                existing.eventType(),
                existing.sourceService(),
                existing.aggregateType(),
                existing.aggregateId(),
                existing.actorId(),
                existing.recipientUserId(),
                existing.payload(),
                NotificationEventStatus.PENDING,
                existing.retryCount(),
                existing.maxRetryCount(),
                existing.lastError(),
                null,
                null,
                existing.createdAt(),
                null
        );
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
                """
                        SELECT reference_type FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryReferenceId(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT reference_id FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryTitle(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT title FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
                String.class,
                eventId,
                userId
        );
    }

    private String queryMetadata(UUID eventId, UUID userId) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT metadata FROM user_notifications
                        WHERE notification_event_id = ? AND user_id = ?
                        """,
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
