package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.PaymentSuccessNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.PaymentSuccessNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentSuccessNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();

    private PaymentSuccessNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new PaymentSuccessNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_usesPaymentReferenceWhenPaymentIdPresent() {
        PaymentSuccessNotificationContext context = parser.parse(event(
                "PAYMENT",
                "pay-1",
                """
                        {
                          "buyer_id":"%s",
                          "payment_id":"pay-1",
                          "order_id":"order-1",
                          "order_code":"ORD-1",
                          "amount":"100000"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertEquals(BUYER_ID, context.buyerId());
        assertEquals("pay-1", context.paymentId());
        assertEquals("PAYMENT", context.referenceType());
        assertEquals("pay-1", context.referenceId());
        assertEquals("ORD-1", context.orderCode());
        assertEquals("100000", context.amountSummary());
    }

    @Test
    void parse_fallsBackToOrderReferenceWhenPaymentIdMissing() {
        PaymentSuccessNotificationContext context = parser.parse(event(
                "ORDER",
                "order-1",
                """
                        {
                          "buyer_id":"%s",
                          "order_id":"order-1",
                          "order_code":"ORD-2"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertEquals("ORDER", context.referenceType());
        assertEquals("order-1", context.referenceId());
    }

    @Test
    void parse_throwsWhenBuyerIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                "PAYMENT",
                "pay-1",
                """
                        {"payment_id":"pay-1","order_id":"order-1"}
                        """
        )));
    }

    @Test
    void parse_throwsWhenPaymentAndOrderReferenceMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                null,
                null,
                """
                        {"buyer_id":"%s"}
                        """.formatted(BUYER_ID)
        )));
    }

    private NotificationEvent event(String aggregateType, String aggregateId, String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "PAYMENT_SUCCESS",
                NotificationSourceService.COMMERCE,
                aggregateType,
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
