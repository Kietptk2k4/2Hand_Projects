package com.twohands.notification_service.integration.handler;

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
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "notification.integrations.email.enabled=true")
class OrderCreatedNotificationIntegrationTest {

    @Autowired
    private ProcessNotificationEventUseCase processNotificationEventUseCase;

    @Autowired
    private NotificationEventRepository notificationEventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.execute("DELETE FROM user_notifications");
        jdbcTemplate.execute("DELETE FROM notification_events");
    }

    @Test
    void process_notifiesBuyerAndSeller() {
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID eventId = insertOrderCreatedEvent(buyerId, sellerId, "order-100", false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, buyerId));
        assertEquals(1, countNotifications(eventId, sellerId));
        assertEquals("ORDER", queryReferenceType(eventId, buyerId));
        assertEquals("order-100", queryReferenceId(eventId, buyerId));
        assertEquals("Đơn hàng mới", queryTitle(eventId, sellerId));
    }

    @Test
    void process_notifiesBuyerOnlyWhenSellerIdsMissing() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = insertOrderCreatedEvent(buyerId, null, "order-solo", true);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, buyerId));
    }

    @Test
    void process_marksEventFailedWhenBuyerMissing() {
        UUID eventId = insertOrderCreatedEvent(null, UUID.randomUUID(), "order-1", false);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    @Test
    void process_isIdempotentForDuplicateDelivery() {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = insertOrderCreatedEvent(buyerId, null, "order-dup", true);

        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));
        notificationEventRepository.save(reopenForReprocess(eventId));
        processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(1, countNotifications(eventId, buyerId));
    }

    private UUID insertOrderCreatedEvent(UUID buyerId, UUID sellerId, String orderId, boolean omitSellers) {
        UUID eventId = UUID.randomUUID();
        String payload;
        if (buyerId == null) {
            payload = """
                    {"order_id":"%s","recipient_email":"buyer@example.com","order_code":"ORD-1"}
                    """.formatted(orderId);
        } else if (omitSellers) {
            payload = """
                    {
                      "buyer_id":"%s",
                      "order_id":"%s",
                      "order_code":"ORD-1",
                      "recipient_email":"buyer@example.com"
                    }
                    """.formatted(buyerId, orderId);
        } else {
            payload = """
                    {
                      "buyer_id":"%s",
                      "order_id":"%s",
                      "order_code":"ORD-1",
                      "recipient_email":"buyer@example.com",
                      "seller_ids":["%s"]
                    }
                    """.formatted(buyerId, orderId, sellerId);
        }

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "ORDER_CREATED",
                NotificationSourceService.COMMERCE,
                "ORDER",
                orderId,
                null,
                buyerId,
                payload,
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
