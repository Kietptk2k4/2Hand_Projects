package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ShipmentDeliveredNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.ShipmentDeliveredNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentDeliveredNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();

    private ShipmentDeliveredNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ShipmentDeliveredNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_usesShipmentReferenceWhenShipmentIdPresent() {
        ShipmentDeliveredNotificationContext context = parser.parse(event(
                """
                        {
                          "buyer_id":"%s",
                          "shipment_id":"ship-1",
                          "order_id":"order-1",
                          "delivered_at":"2026-05-25T10:00:00Z"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertEquals("SHIPMENT", context.referenceType());
        assertEquals("ship-1", context.referenceId());
        assertEquals("2026-05-25T10:00:00Z", context.deliveredAt());
    }

    @Test
    void parse_fallsBackToOrderReferenceWhenShipmentIdMissing() {
        ShipmentDeliveredNotificationContext context = parser.parse(eventWithoutShipmentAggregate(
                """
                        {
                          "buyer_id":"%s",
                          "order_id":"order-1"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertEquals("ORDER", context.referenceType());
        assertEquals("order-1", context.referenceId());
    }

    @Test
    void parse_throwsWhenBuyerIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                """
                        {"shipment_id":"ship-1","order_id":"order-1"}
                        """
        )));
    }

    @Test
    void parse_throwsWhenShipmentAndOrderReferenceMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutReferences(
                """
                        {"buyer_id":"%s"}
                        """.formatted(BUYER_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHIPMENT_DELIVERED",
                NotificationSourceService.COMMERCE,
                "SHIPMENT",
                "ship-1",
                null,
                null,
                payload,
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }

    private NotificationEvent eventWithoutShipmentAggregate(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHIPMENT_DELIVERED",
                NotificationSourceService.COMMERCE,
                "ORDER",
                "order-1",
                null,
                null,
                payload,
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }

    private NotificationEvent eventWithoutReferences(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHIPMENT_DELIVERED",
                NotificationSourceService.COMMERCE,
                null,
                null,
                null,
                null,
                payload,
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }
}
