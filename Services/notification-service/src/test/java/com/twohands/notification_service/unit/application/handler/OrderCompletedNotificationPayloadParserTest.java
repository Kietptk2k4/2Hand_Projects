package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.OrderCompletedNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.OrderCompletedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCompletedNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();

    private OrderCompletedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new OrderCompletedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesBuyerAndOrderReference() {
        OrderCompletedNotificationContext context = parser.parse(event(
                """
                        {
                          "buyer_id":"%s",
                          "order_id":"order-1",
                          "order_code":"ORD-1",
                          "completed_at":"2026-05-25T12:00:00Z"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertEquals(BUYER_ID, context.buyerId());
        assertEquals("order-1", context.orderId());
        assertEquals("ORD-1", context.orderCode());
        assertEquals("2026-05-25T12:00:00Z", context.completedAt());
    }

    @Test
    void parse_throwsWhenBuyerIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                """
                        {"order_id":"order-1"}
                        """
        )));
    }

    @Test
    void parse_throwsWhenOrderIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutOrderAggregate(
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
                "ORDER_COMPLETED",
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

    private NotificationEvent eventWithoutOrderAggregate(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "ORDER_COMPLETED",
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
