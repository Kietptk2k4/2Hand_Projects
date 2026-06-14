package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.OrderCancelPendingRefundNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.OrderCancelPendingRefundNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCancelPendingRefundNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID REFUND_REQUEST_ID = UUID.randomUUID();

    private OrderCancelPendingRefundNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new OrderCancelPendingRefundNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesBuyerOrderAndRefundRequest() {
        OrderCancelPendingRefundNotificationContext context = parser.parse(event(
                """
                        {
                          "buyer_id":"%s",
                          "order_id":"%s",
                          "refund_request_id":"%s"
                        }
                        """.formatted(BUYER_ID, ORDER_ID, REFUND_REQUEST_ID)
        ));

        assertEquals(BUYER_ID, context.buyerId());
        assertEquals(ORDER_ID.toString(), context.orderId());
        assertEquals(REFUND_REQUEST_ID.toString(), context.refundRequestId());
    }

    @Test
    void parse_fallsBackToAggregateIdForOrder() {
        OrderCancelPendingRefundNotificationContext context = parser.parse(event(
                """
                        {"buyer_id":"%s"}
                        """.formatted(BUYER_ID),
                ORDER_ID.toString()
        ));

        assertEquals(ORDER_ID.toString(), context.orderId());
    }

    @Test
    void parse_throwsWhenBuyerIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                """
                        {"order_id":"%s"}
                        """.formatted(ORDER_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return event(payload, ORDER_ID.toString());
    }

    private NotificationEvent event(String payload, String aggregateId) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "ORDER_CANCEL_PENDING_REFUND",
                NotificationSourceService.COMMERCE,
                "ORDER",
                aggregateId,
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
