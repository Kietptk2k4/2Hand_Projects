package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.PaymentFailedNotificationPayloadParser;
import com.twohands.notification_service.domain.commerce.PaymentFailedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentFailedNotificationPayloadParserTest {

    private static final UUID BUYER_ID = UUID.randomUUID();

    private PaymentFailedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new PaymentFailedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_usesStoredUserFailureReason() {
        PaymentFailedNotificationContext context = parser.parse(event(
                """
                        {
                          "buyer_id":"%s",
                          "payment_id":"pay-1",
                          "order_id":"order-1",
                          "user_failure_reason":"Insufficient balance"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertEquals("Insufficient balance", context.userFacingFailureReason());
        assertEquals("PAYMENT", context.referenceType());
    }

    @Test
    void parse_omitsUnsafeFailureReason() {
        PaymentFailedNotificationContext context = parser.parse(event(
                """
                        {
                          "buyer_id":"%s",
                          "payment_id":"pay-1",
                          "failure_reason":"stripe internal webhook error"
                        }
                        """.formatted(BUYER_ID)
        ));

        assertNull(context.userFacingFailureReason());
    }

    @Test
    void parse_throwsWhenBuyerIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                """
                        {"payment_id":"pay-1","order_id":"order-1"}
                        """
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "PAYMENT_FAILED",
                NotificationSourceService.COMMERCE,
                "PAYMENT",
                "pay-1",
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
