package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.handler.EmailNotificationEventHandler;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationRecipientResolver;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    @Mock
    private NotificationRecipientResolver recipientResolver;

    @Mock
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    private EmailNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EmailNotificationEventHandler(recipientResolver, sendEmailNotificationUseCase);
    }

    @Test
    void supports_emailEligibleEventsExceptDedicatedHandlers() {
        assertFalse(handler.supports("EMAIL_VERIFICATION_REQUESTED"));
        assertFalse(handler.supports("PASSWORD_RESET_REQUESTED"));
        assertFalse(handler.supports("USER_SUSPENDED"));
        assertFalse(handler.supports("USER_RESTRICTED"));
        assertFalse(handler.supports("ORDER_CREATED"));
        assertFalse(handler.supports("PAYMENT_SUCCESS"));
        assertFalse(handler.supports("COMMERCE_PAYMENT_PAID"));
        assertFalse(handler.supports("SHOP_SUSPENDED"));
        assertFalse(handler.supports("USER_CREATED"));
        assertFalse(handler.supports("POST_LIKED"));
    }

    @Test
    void handle_returnsSuccessWhenEmailSent() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.sent("msg-1"));

        var result = handler.handle(sampleEvent("ORDER_CREATED"));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
    }

    @Test
    void handle_returnsNoOpWhenEmailSkipped() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.skipped("Email integration is disabled."));

        var result = handler.handle(sampleEvent("ORDER_CREATED"));

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
    }

    @Test
    void handle_returnsFailureWhenEmailSendFails() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.failed(
                        NotificationFailurePolicy.PERMANENT,
                        "Missing required template variable: order_code"
                ));

        var result = handler.handle(sampleEvent("ORDER_CREATED"));

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent(String eventType) {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                RECIPIENT_ID,
                "{}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        );
    }
}
