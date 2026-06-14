package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.CommerceReviewReminderPayloadNormalizer;
import com.twohands.notification_service.application.ingest.NotificationEventIngestCommand;
import com.twohands.notification_service.application.ingest.StoreNotificationEventUseCase;
import com.twohands.notification_service.application.worker.ProcessNotificationEventCommand;
import com.twohands.notification_service.application.worker.ProcessNotificationEventOutcome;
import com.twohands.notification_service.application.worker.ProcessNotificationEventUseCase;
import com.twohands.notification_service.domain.commerce.ReviewReminderEventKeyPolicy;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ReviewReminderNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private StoreNotificationEventUseCase storeNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommerceReviewReminderPayloadNormalizer commerceReviewReminderPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesBuyerWithProductReference() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = ingestReviewReminderEvent(buyerId, "item-100", "prod-200", 7, false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, buyerId));
        assertEquals("PRODUCT", queryReferenceType(eventId, buyerId));
        assertEquals("prod-200", queryReferenceId(eventId, buyerId));
        assertEquals("Nhắc đánh giá", queryTitle(eventId, buyerId));
    }

    @Test
    void process_completesWithoutNotificationWhenAlreadyReviewed() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = ingestReviewReminderEvent(buyerId, "item-reviewed", "prod-1", 3, true);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(0, countNotifications(eventId, buyerId));
    }

    @Test
    void process_marksEventFailedWhenBuyerMissing() {
        UUID eventId = insertReviewReminderEvent(null, "item-x", false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void ingest_isIdempotentByEventKey() {
        UUID buyerId = UUID.randomUUID();
        String orderItemId = "item-dup";
        int reminderDay = 14;
        String eventKey = ReviewReminderEventKeyPolicy.build(orderItemId, reminderDay);

        var first = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                eventKey,
                "REVIEW_REMINDER",
                NotificationSourceService.COMMERCE,
                "ORDER_ITEM",
                orderItemId,
                null,
                buyerId,
                reviewReminderPayload(buyerId, orderItemId, "prod-dup", reminderDay, false)
        ));
        var second = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                eventKey,
                "REVIEW_REMINDER",
                NotificationSourceService.COMMERCE,
                "ORDER_ITEM",
                orderItemId,
                null,
                buyerId,
                reviewReminderPayload(buyerId, orderItemId, "prod-dup", reminderDay, false)
        ));

        assertEquals(false, first.duplicate());
        assertEquals(first.notificationEventId(), second.notificationEventId());
        assertTrue(second.duplicate());
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = ingestReviewReminderEvent(buyerId, "item-retry", "prod-retry", 1, false);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, buyerId));
    }

    private UUID ingestReviewReminderEvent(
            UUID buyerId,
            String orderItemId,
            String productId,
            int reminderDay,
            boolean alreadyReviewed
    ) {
        var ingestResult = storeNotificationEventUseCase.execute(new NotificationEventIngestCommand(
                UUID.randomUUID(),
                ReviewReminderEventKeyPolicy.build(orderItemId, reminderDay),
                "REVIEW_REMINDER",
                NotificationSourceService.COMMERCE,
                "ORDER_ITEM",
                orderItemId,
                null,
                buyerId,
                reviewReminderPayload(buyerId, orderItemId, productId, reminderDay, alreadyReviewed)
        ));
        return ingestResult.notificationEventId();
    }

    private String reviewReminderPayload(
            UUID buyerId,
            String orderItemId,
            String productId,
            int reminderDay,
            boolean alreadyReviewed
    ) {
        return """
                {
                  "buyer_id":"%s",
                  "order_item_id":"%s",
                  "order_id":"order-500",
                  "order_code":"ORD-500",
                  "product_id":"%s",
                  "product_name":"Sample Product",
                  "reminder_day":%d,
                  "already_reviewed":%s
                }
                """.formatted(buyerId, orderItemId, productId, reminderDay, alreadyReviewed);
    }

    private UUID insertReviewReminderEvent(UUID buyerId, String orderItemId, boolean alreadyReviewed) {
        UUID eventId = UUID.randomUUID();
        String payload = buyerId == null
                ? """
                {
                  "order_item_id":"%s",
                  "order_id":"order-500",
                  "reminder_day":7
                }
                """.formatted(orderItemId)
                : reviewReminderPayload(buyerId, orderItemId, "prod-x", 7, alreadyReviewed);
        String storedPayload = commerceReviewReminderPayloadNormalizer.normalizeForStorage(
                "REVIEW_REMINDER",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                ReviewReminderEventKeyPolicy.build(orderItemId, 7),
                "REVIEW_REMINDER",
                NotificationSourceService.COMMERCE,
                "ORDER_ITEM",
                orderItemId,
                null,
                buyerId,
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

    private String queryEventStatus(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM notification_events WHERE id = ?",
                String.class,
                eventId
        );
    }
}
