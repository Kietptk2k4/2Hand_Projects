package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.InAppSocialNotificationEventHandler;
import com.twohands.notification_service.application.handler.NotificationDeliveryChannelPolicy;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.NotificationRecipientResolver;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
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
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    private InAppSocialNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InAppSocialNotificationEventHandler(
                new NotificationDeliveryChannelPolicy(),
                new NotificationRecipientResolver(new ObjectMapper()),
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase
        );
    }

    @Test
    void handle_createsInAppNotificationForRecipient() {
        UUID eventId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        NotificationEvent event = sampleEvent(eventId, actorId, recipientId);

        when(applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand("POST_LIKED", NotificationSourceService.SOCIAL, actorId, recipientId)
        )).thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(recipientId, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(createInAppNotificationUseCase.execute(any(CreateInAppNotificationCommand.class)))
                .thenReturn(new CreateInAppNotificationResult(UUID.randomUUID(), false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());

        ArgumentCaptor<CreateInAppNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateInAppNotificationCommand.class);
        verify(createInAppNotificationUseCase).execute(captor.capture());
        assertEquals(recipientId, captor.getValue().userId());
        assertEquals(eventId, captor.getValue().notificationEventId());
    }

    @Test
    void handle_returnsNoOpWhenSelfNotificationSkipped() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), userId, userId);

        when(applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand("POST_LIKED", NotificationSourceService.SOCIAL, userId, userId)
        )).thenReturn(SkipSelfNotificationOutcome.SKIP);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(applyNotificationDeliveryRulesUseCase, never()).execute(any());
        verify(createInAppNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsRetryableFailureWhenActorMissingForSelfSkipEvent() {
        UUID recipientId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), null, recipientId);

        when(applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand("POST_LIKED", NotificationSourceService.SOCIAL, null, recipientId)
        )).thenReturn(SkipSelfNotificationOutcome.MISSING_ACTOR);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
        verify(createInAppNotificationUseCase, never()).execute(any());
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
        verify(createInAppNotificationUseCase, never()).execute(any());
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
