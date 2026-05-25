package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.ShipmentCreatedNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.ShipmentCreatedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShipmentCreatedNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    private ShipmentCreatedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new ShipmentCreatedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesBuyerSellerAndShipmentReference() {
        ShipmentCreatedNotificationContext context = parser.parse(event(
                """
                        {
                          "buyer_id":"%s",
                          "seller_id":"%s",
                          "shipment_id":"ship-1",
                          "order_id":"order-1",
                          "tracking_code":"VN999"
                        }
                        """.formatted(BUYER_ID, SELLER_ID)
        ));

        assertEquals(BUYER_ID, context.buyerId());
        assertEquals(SELLER_ID, context.sellerId());
        assertEquals("ship-1", context.shipmentId());
        assertEquals("order-1", context.orderId());
        assertEquals("VN999", context.trackingCode());
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
    void parse_throwsWhenShipmentIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutShipmentAggregate(
                """
                        {"buyer_id":"%s","order_id":"order-1"}
                        """.formatted(BUYER_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHIPMENT_CREATED",
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
                "SHIPMENT_CREATED",
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
}
