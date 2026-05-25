package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.PostLikedNotificationEventHandler;
import com.twohands.notification_service.application.handler.PostLikedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.PostLikedNotificationContext;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostLikedNotificationEventHandlerTest {

    @Mock
    private PostLikedNotificationPayloadParser payloadParser;

    @Mock
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private PostLikedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PostLikedNotificationEventHandler(
                payloadParser,
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_onlyPostLiked() {
        assertTrue(handler.supports("POST_LIKED"));
        assertFalse(handler.supports("USER_FOLLOWED"));
    }

    @Test
    void handle_createsInAppAndSendsPushForExternalLike() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, actorId, postAuthorId);

        when(payloadParser.parse(event)).thenReturn(new PostLikedNotificationContext(actorId, postAuthorId, "post-1"));
        when(applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand("POST_LIKED", NotificationSourceService.SOCIAL, actorId, postAuthorId)
        )).thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(postAuthorId, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(createInAppNotificationUseCase.execute(any(CreateInAppNotificationCommand.class)))
                .thenReturn(new CreateInAppNotificationResult(UUID.randomUUID(), false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(any(CreateInAppNotificationCommand.class));
        verify(sendPushNotificationUseCase).execute(any(SendPushNotificationCommand.class));
    }

    @Test
    void handle_returnsNoOpWhenSelfLike() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), userId, userId);

        when(payloadParser.parse(event)).thenReturn(new PostLikedNotificationContext(userId, userId, "post-1"));
        when(applySkipSelfNotificationUseCase.execute(any(ApplySkipSelfNotificationCommand.class)))
                .thenReturn(SkipSelfNotificationOutcome.SKIP);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(createInAppNotificationUseCase, never()).execute(any());
        verify(sendPushNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsPermanentFailureWhenPayloadInvalid() {
        NotificationEvent event = sampleEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(payloadParser.parse(event))
                .thenThrow(new IllegalArgumentException("post_author_id is required for POST_LIKED notification event."));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    @Test
    void handle_skipsPushWhenPushDisabledBySettings() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, actorId, postAuthorId);

        when(payloadParser.parse(event)).thenReturn(new PostLikedNotificationContext(actorId, postAuthorId, "post-9"));
        when(applySkipSelfNotificationUseCase.execute(any(ApplySkipSelfNotificationCommand.class)))
                .thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(postAuthorId, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(true, false, false));
        when(createInAppNotificationUseCase.execute(any(CreateInAppNotificationCommand.class)))
                .thenReturn(new CreateInAppNotificationResult(UUID.randomUUID(), false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(sendPushNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsNoOpWhenAllChannelsDisabled() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), actorId, postAuthorId);

        when(payloadParser.parse(event)).thenReturn(new PostLikedNotificationContext(actorId, postAuthorId, "post-1"));
        when(applySkipSelfNotificationUseCase.execute(any(ApplySkipSelfNotificationCommand.class)))
                .thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(postAuthorId, "POST_LIKED")
        )).thenReturn(new NotificationDeliveryDecision(false, false, false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(createInAppNotificationUseCase, never()).execute(any());
        verify(sendPushNotificationUseCase, never()).execute(any());
    }

    private NotificationEvent sampleEvent(UUID eventId, UUID actorId, UUID recipientId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-1",
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
