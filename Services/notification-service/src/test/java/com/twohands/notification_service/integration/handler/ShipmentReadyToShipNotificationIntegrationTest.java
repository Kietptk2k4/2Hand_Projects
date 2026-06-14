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

@SpringBootTest
@ActiveProfiles("test")
class ShipmentReadyToShipNotificationIntegrationTest {

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
        UUID eventId = insertShipmentReadyToShipEvent(buyerId);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.COMPLETED, result.outcome());
        assertEquals(1, countNotifications(eventId, buyerId));
        assertEquals("SHIPMENT", queryReferenceType(eventId, buyerId));
        assertEquals("ship-100", queryReferenceId(eventId, buyerId));
        assertEquals("Đơn sẵn sàng giao", queryTitle(eventId, buyerId));
    }

    @Test
    void process_marksEventFailedWhenBuyerMissing() {
        UUID eventId = insertShipmentReadyToShipEvent(null);

        var result = processNotificationEventUseCase.execute(new ProcessNotificationEventCommand(eventId));

        assertEquals(ProcessNotificationEventOutcome.FAILED, result.outcome());
        assertEquals("FAILED", queryEventStatus(eventId));
    }

    private UUID insertShipmentReadyToShipEvent(UUID buyerId) {
        UUID eventId = UUID.randomUUID();
        String payload;
        if (buyerId == null) {
            payload = """
                    {
                      "shipment_id":"ship-100",
                      "order_id":"order-100",
                      "tracking_code":"VN123"
                    }
                    """;
        } else {
            payload = """
                    {
                      "buyer_id":"%s",
                      "shipment_id":"ship-100",
                      "order_id":"order-100",
                      "tracking_code":"VN123",
                      "carrier_raw_response":"{}"
                    }
                    """.formatted(buyerId);
        }

        String storedPayload = commerceShipmentNotificationPayloadNormalizer.normalizeForStorage(
                "SHIPMENT_READY_TO_SHIP",
                payload
        );

        notificationEventRepository.save(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "SHIPMENT_READY_TO_SHIP",
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
