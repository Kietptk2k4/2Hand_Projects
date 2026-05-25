package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.PaymentSuccessEmailNotificationEventHandler;
import com.twohands.notification_service.application.handler.PaymentSuccessNotificationPayloadParser;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.PaymentSuccessNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentSuccessEmailNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();

    @Mock
    private PaymentSuccessNotificationPayloadParser payloadParser;

    @Mock
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    private PaymentSuccessEmailNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PaymentSuccessEmailNotificationEventHandler(
                new NotificationEventTypeAliasResolver(),
                payloadParser,
                sendEmailNotificationUseCase
        );
    }

    @Test
    void supports_paymentSuccessEventTypes() {
        assertTrue(handler.supports("PAYMENT_SUCCESS"));
        assertTrue(handler.supports("COMMERCE_PAYMENT_PAID"));
        assertFalse(handler.supports("PAYMENT_FAILED"));
    }

    @Test
    void handle_sendsPaymentSuccessEmailToBuyer() {
        when(payloadParser.parse(any())).thenReturn(new PaymentSuccessNotificationContext(
                BUYER_ID,
                "pay-1",
                "order-1",
                "ORD-1",
                "PAYMENT",
                "pay-1",
                "100000"
        ));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.sent("email-1"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(sendEmailNotificationUseCase).execute(new SendEmailNotificationCommand(
                BUYER_ID,
                "PAYMENT_SUCCESS",
                sampleEvent().payload()
        ));
    }

    @Test
    void handle_returnsNoOpWhenEmailSkipped() {
        when(payloadParser.parse(any())).thenReturn(new PaymentSuccessNotificationContext(
                BUYER_ID,
                "pay-1",
                "order-1",
                "ORD-1",
                "PAYMENT",
                "pay-1",
                "100000"
        ));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.skipped("Email channel disabled by delivery policy."));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
    }

    @Test
    void handle_returnsFailureWhenBuyerIdMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("buyer_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    @Test
    void handle_returnsFailureWhenEmailSendFails() {
        when(payloadParser.parse(any())).thenReturn(new PaymentSuccessNotificationContext(
                BUYER_ID,
                "pay-1",
                "order-1",
                "ORD-1",
                "PAYMENT",
                "pay-1",
                "100000"
        ));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.failed(
                        NotificationFailurePolicy.PERMANENT,
                        "Missing required template variable: recipient_email"
                ));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "PAYMENT_SUCCESS",
                NotificationSourceService.COMMERCE,
                "PAYMENT",
                "pay-1",
                null,
                BUYER_ID,
                """
                        {
                          "buyer_id":"%s",
                          "payment_id":"pay-1",
                          "order_id":"order-1",
                          "order_code":"ORD-1",
                          "recipient_email":"buyer@example.com",
                          "amount":"100000",
                          "payment_method":"COD"
                        }
                        """.formatted(BUYER_ID),
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
