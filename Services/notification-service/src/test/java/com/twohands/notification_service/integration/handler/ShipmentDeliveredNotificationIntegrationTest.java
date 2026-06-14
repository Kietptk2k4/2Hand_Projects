package com.twohands.notification_service.integration.handler;

import com.twohands.notification_service.application.email.CommerceShipmentNotificationPayloadNormalizer;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ShipmentDeliveredNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommerceShipmentNotificationPayloadNormalizer commerceShipmentNotificationPayloadNormalizer;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesBuyerWithShipmentReference() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = insertShipmentDeliveredEvent(buyerId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, buyerId));
        assertEquals("SHIPMENT", queryReferenceType(eventId, buyerId));
        assertEquals("ship-100", queryReferenceId(eventId, buyerId));
        assertEquals("Giao hàng thành công", queryTitle(eventId, buyerId));
        assertTrue(queryMetadata(eventId, buyerId).contains("delivered_at"));
        assertTrue(queryMetadata(eventId, buyerId).contains("prompt_confirm_receipt"));
    }

    @Test
    void process_marksEventFailedWhenBuyerMissing() {
        UUID eventId = insertShipmentDeliveredEvent(null);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = insertShipmentDeliveredEvent(buyerId);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, buyerId));
    }

    private UUID insertShipmentDeliveredEvent(UUID buyerId) {
        UUID eventId = UUID.randomUUID();
        String payload;
        if (buyerId == null) {
            payload = """
                    {
                      "shipment_id":"ship-100",
                      "order_id":"order-100",
                      "delivered_at":"2026-05-25T10:00:00Z"
                    }
                    """;
        } else {
            payload = """
                    {
                      "buyer_id":"%s",
                      "shipment_id":"ship-100",
                      "order_id":"order-100",
                      "delivered_at":"2026-05-25T10:00:00Z",
                      "show_confirm_receipt":true,
                      "carrier_raw_response":"{}"
                    }
                    """.formatted(buyerId);
        }

        String storedPayload = commerceShipmentNotificationPayloadNormalizer.normalizeForStorage(
                "SHIPMENT_DELIVERED",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "SHIPMENT_DELIVERED",
                NotificationSourceService.COMMERCE,
                "SHIPMENT",
                "ship-100",
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
