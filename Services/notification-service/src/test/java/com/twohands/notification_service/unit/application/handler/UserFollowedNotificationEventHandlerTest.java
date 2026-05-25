package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.UserFollowedNotificationEventHandler;
import com.twohands.notification_service.application.handler.UserFollowedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.UserFollowedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UserFollowedNotificationEventHandlerTest {

    @Mock
    private UserFollowedNotificationPayloadParser payloadParser;

    @Mock
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private UserFollowedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserFollowedNotificationEventHandler(
                payloadParser,
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_onlyUserFollowed() {
        assertTrue(handler.supports("USER_FOLLOWED"));
        assertFalse(handler.supports("POST_LIKED"));
    }

    @Test
    void handle_createsInAppWithUserActorReference() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, actorId, followedUserId);

        when(payloadParser.parse(event)).thenReturn(new UserFollowedNotificationContext(actorId, followedUserId));
        when(applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand("USER_FOLLOWED", NotificationSourceService.SOCIAL, actorId, followedUserId)
        )).thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(followedUserId, "USER_FOLLOWED")
        )).thenReturn(new NotificationDeliveryDecision(true, false, false));
        when(createInAppNotificationUseCase.execute(any(CreateInAppNotificationCommand.class)))
                .thenReturn(new CreateInAppNotificationResult(UUID.randomUUID(), false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());

        ArgumentCaptor<CreateInAppNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateInAppNotificationCommand.class);
        verify(createInAppNotificationUseCase).execute(captor.capture());
        assertEquals("USER", captor.getValue().referenceType());
        assertEquals(actorId.toString(), captor.getValue().referenceId());
        assertEquals(followedUserId, captor.getValue().userId());
        verify(sendPushNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsNoOpWhenSelfFollow() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), userId, userId);

        when(payloadParser.parse(event)).thenReturn(new UserFollowedNotificationContext(userId, userId));
        when(applySkipSelfNotificationUseCase.execute(any(ApplySkipSelfNotificationCommand.class)))
                .thenReturn(SkipSelfNotificationOutcome.SKIP);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(createInAppNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsPermanentFailureWhenFollowedUserMissing() {
        NotificationEvent event = sampleEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(payloadParser.parse(event))
                .thenThrow(new IllegalArgumentException("followed_user_id is required for USER_FOLLOWED notification event."));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent(UUID eventId, UUID actorId, UUID followedUserId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "USER_FOLLOWED",
                NotificationSourceService.SOCIAL,
                "USER",
                actorId != null ? actorId.toString() : null,
                actorId,
                followedUserId,
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
