package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.UserAvatarUpdatedNotificationEventHandler;
import com.twohands.notification_service.application.handler.UserAvatarUpdatedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.UserAvatarUpdatedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarUpdatedNotificationEventHandlerTest {

    @Mock
    private UserAvatarUpdatedNotificationPayloadParser payloadParser;

    @Mock
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private UserAvatarUpdatedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserAvatarUpdatedNotificationEventHandler(
                payloadParser,
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_onlyUserAvatarUpdated() {
        assertTrue(handler.supports("USER_AVATAR_UPDATED"));
        assertFalse(handler.supports("USER_FOLLOWED"));
    }

    @Test
    void handle_notifiesEachFollower() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID followerA = UUID.randomUUID();
        UUID followerB = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, actorId);

        when(payloadParser.parse(event)).thenReturn(new UserAvatarUpdatedNotificationContext(
                actorId,
                "https://cdn.2hands.vn/new.png",
                "User A",
                List.of(followerA, followerB)
        ));
        when(applySkipSelfNotificationUseCase.execute(any(ApplySkipSelfNotificationCommand.class)))
                .thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, false, false));
        when(createInAppNotificationUseCase.execute(any(CreateInAppNotificationCommand.class)))
                .thenReturn(new CreateInAppNotificationResult(UUID.randomUUID(), false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());

        ArgumentCaptor<CreateInAppNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateInAppNotificationCommand.class);
        verify(createInAppNotificationUseCase, org.mockito.Mockito.times(2)).execute(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(cmd -> followerA.equals(cmd.userId())));
        assertTrue(captor.getAllValues().stream().anyMatch(cmd -> followerB.equals(cmd.userId())));
    }

    private NotificationEvent sampleEvent(UUID eventId, UUID actorId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "USER_AVATAR_UPDATED",
                NotificationSourceService.SOCIAL,
                "USER",
                actorId.toString(),
                actorId,
                null,
                """
                        {
                          "actor_id":"%s",
                          "avatar_url":"https://cdn.2hands.vn/new.png",
                          "follower_user_ids":["%s"]
                        }
                        """.formatted(actorId, UUID.randomUUID()),
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        );
    }
}
