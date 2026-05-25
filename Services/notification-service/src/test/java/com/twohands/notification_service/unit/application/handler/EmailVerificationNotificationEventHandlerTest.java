package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.handler.EmailVerificationNotificationEventHandler;
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
class EmailVerificationNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    @Mock
    private NotificationRecipientResolver recipientResolver;

    @Mock
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    private EmailVerificationNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EmailVerificationNotificationEventHandler(recipientResolver, sendEmailNotificationUseCase);
    }

    @Test
    void supports_onlyEmailVerificationRequested() {
        assertTrue(handler.supports("EMAIL_VERIFICATION_REQUESTED"));
        assertFalse(handler.supports("PASSWORD_RESET_REQUESTED"));
        assertFalse(handler.supports("POST_LIKED"));
    }

    @Test
    void handle_returnsSuccessWhenEmailSent() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.sent("msg-1"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
    }

    @Test
    void handle_returnsFailureWhenRecipientMissing() {
        when(recipientResolver.resolve(any())).thenReturn(List.of());

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
    }

    @Test
    void handle_returnsFailureWhenEmailSendFails() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.failed(
                        NotificationFailurePolicy.PERMANENT,
                        "Missing required template variable: verification_link"
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
                "EMAIL_VERIFICATION_REQUESTED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                RECIPIENT_ID,
                """
                        {
                          "recipient_email": "user@example.com",
                          "verification_link": "https://2hands.vn/verify-email?token=abc"
                        }
                        """,
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
