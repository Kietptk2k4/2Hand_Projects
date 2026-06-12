package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.handler.AccountEnforcementNotificationEventHandler;
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
class AccountEnforcementNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    @Mock
    private NotificationRecipientResolver recipientResolver;

    @Mock
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    private AccountEnforcementNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AccountEnforcementNotificationEventHandler(recipientResolver, sendEmailNotificationUseCase);
    }

    @Test
    void supports_accountEnforcementEmailEventsOnly() {
        assertTrue(handler.supports("USER_SUSPENDED"));
        assertTrue(handler.supports("USER_BANNED"));
        assertTrue(handler.supports("USER_RESTRICTED"));
        assertFalse(handler.supports("SHOP_SUSPENDED"));
        assertFalse(handler.supports("POST_LIKED"));
    }

    @Test
    void handle_returnsSuccessWhenEmailSent() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.sent("msg-1"));

        var result = handler.handle(sampleEvent("USER_SUSPENDED"));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
    }

    @Test
    void handle_returnsFailureWhenRecipientMissing() {
        when(recipientResolver.resolve(any())).thenReturn(List.of());

        var result = handler.handle(sampleEvent("USER_RESTRICTED"));

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
    }

    private NotificationEvent sampleEvent(String eventType) {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.ADMIN,
                null,
                null,
                null,
                RECIPIENT_ID,
                """
                        {
                          "target_user_id": "%s",
                          "recipient_email": "user@example.com",
                          "enforcement_reason": "Repeated policy violations",
                          "enforcement_expires_at": "2026-12-31T00:00:00Z"
                        }
                        """.formatted(RECIPIENT_ID),
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
