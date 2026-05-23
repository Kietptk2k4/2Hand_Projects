package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.InAppSocialNotificationEventHandler;
import com.twohands.notification_service.application.handler.NotificationContentTemplateService;
import com.twohands.notification_service.application.handler.NotificationDeliveryChannelPolicy;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.NotificationRecipientResolver;
import com.twohands.notification_service.application.handler.SkipSelfNotificationPolicy;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationCommand;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationResult;
import com.twohands.notification_service.application.idempotency.CreateIdempotentUserNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InAppSocialNotificationEventHandlerTest {

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateIdempotentUserNotificationUseCase createIdempotentUserNotificationUseCase;

    private InAppSocialNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InAppSocialNotificationEventHandler(
                new NotificationDeliveryChannelPolicy(),
                new NotificationRecipientResolver(new ObjectMapper()),
                new SkipSelfNotificationPolicy(),
                new NotificationContentTemplateService(),
                applyNotificationDeliveryRulesUseCase,
                createIdempotentUserNotificationUseCase
        );
    }

    @Test
    void handle_createsInAppNotificationWithSentDeliveryStatus() {
        UUID eventId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        NotificationEvent event = sampleEvent(eventId, actorId, recipientId);

        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(recipientId, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(createIdempotentUserNotificationUseCase.execute(any(CreateIdempotentUserNotificationCommand.class)))
                .thenReturn(new CreateIdempotentUserNotificationResult(UUID.randomUUID(), false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());

        ArgumentCaptor<CreateIdempotentUserNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateIdempotentUserNotificationCommand.class);
        verify(createIdempotentUserNotificationUseCase).execute(captor.capture());
        assertEquals(recipientId, captor.getValue().userId());
        assertEquals(NotificationDeliveryStatus.SENT, captor.getValue().deliveryStatus());
    }

    @Test
    void handle_returnsNoOpWhenDeliveryRulesDisableInApp() {
        UUID recipientId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), UUID.randomUUID(), recipientId);

        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(recipientId, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(false, false, false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(createIdempotentUserNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsNoOpWhenSelfNotificationSkipped() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), userId, userId);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(applyNotificationDeliveryRulesUseCase, never()).execute(any());
        verify(createIdempotentUserNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsRetryableFailureWhenRecipientMissing() {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
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
        verify(createIdempotentUserNotificationUseCase, never()).execute(any());
    }

    private NotificationEvent sampleEvent(UUID eventId, UUID actorId, UUID recipientId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                actorId,
                recipientId,
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
