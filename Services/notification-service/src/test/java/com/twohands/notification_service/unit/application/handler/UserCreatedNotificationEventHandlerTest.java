package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.NotificationRecipientResolver;
import com.twohands.notification_service.application.handler.UserCreatedNotificationEventHandler;
import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsResult;
import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCreatedNotificationEventHandlerTest {

    @Mock
    private InitializeDefaultNotificationSettingsUseCase initializeDefaultNotificationSettingsUseCase;

    private UserCreatedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserCreatedNotificationEventHandler(
                new NotificationRecipientResolver(new ObjectMapper()),
                initializeDefaultNotificationSettingsUseCase
        );
    }

    @Test
    void handle_initializesDefaultSettingsForUserCreatedEvent() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "USER_CREATED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                "{\"user_id\":\"" + userId + "\"}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        );

        when(initializeDefaultNotificationSettingsUseCase.execute(any(InitializeDefaultNotificationSettingsCommand.class)))
                .thenReturn(new InitializeDefaultNotificationSettingsResult(userId, 22, 0));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(initializeDefaultNotificationSettingsUseCase).execute(
                new InitializeDefaultNotificationSettingsCommand(userId)
        );
    }

    @Test
    void handle_returnsRetryableFailureWhenUserIdMissing() {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "USER_CREATED",
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                null,
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

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
    }
}
