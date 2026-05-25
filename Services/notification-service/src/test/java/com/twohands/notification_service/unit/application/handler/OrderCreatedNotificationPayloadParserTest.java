package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.OrderCreatedNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.OrderCreatedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCreatedNotificationPayloadParserTest {

    private OrderCreatedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new OrderCreatedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesBuyerOrderAndSellersFromPayload() {
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        OrderCreatedNotificationContext context = parser.parse(event(
                buyerId,
                """
                        {
                          "buyer_id":"%s",
                          "order_id":"order-100",
                          "order_code":"ORD-100",
                          "seller_ids":["%s"],
                          "final_amount":"150000"
                        }
                        """.formatted(buyerId, sellerId)
        ));

        assertEquals(buyerId, context.buyerId());
        assertEquals("order-100", context.orderId());
        assertEquals("ORD-100", context.orderCode());
        assertEquals(1, context.sellerIds().size());
        assertEquals(sellerId, context.sellerIds().getFirst());
        assertEquals("150000", context.totalAmountSummary());
    }

    @Test
    void parse_fallsBackToAggregateIdForOrderId() {
        UUID buyerId = UUID.randomUUID();

        OrderCreatedNotificationContext context = parser.parse(new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "ORDER_CREATED",
                NotificationSourceService.COMMERCE,
                "ORDER",
                "order-aggregate",
                null,
                buyerId,
                """
                        {"buyer_id":"%s"}
                        """.formatted(buyerId),
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        ));

        assertEquals("order-aggregate", context.orderId());
        assertEquals("order-aggregate", context.orderCode());
    }

    @Test
    void parse_requiresBuyerId() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                null,
                """
                        {"order_id":"order-1"}
                        """
        )));
    }

    private NotificationEvent event(UUID buyerId, String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "ORDER_CREATED",
                NotificationSourceService.COMMERCE,
                "ORDER",
                "order-1",
                null,
                buyerId,
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
