package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.handler.PostCreatedNotificationEventHandler;
import com.twohands.notification_service.application.handler.PostCreatedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.PostCreatedNotificationContext;
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
class PostCreatedNotificationEventHandlerTest {

    @Mock
    private PostCreatedNotificationPayloadParser payloadParser;

    @Mock
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private PostCreatedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PostCreatedNotificationEventHandler(
                payloadParser,
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_onlyPostCreated() {
        assertTrue(handler.supports("POST_CREATED"));
        assertFalse(handler.supports("POST_LIKED"));
    }

    @Test
    void handle_notifiesEachFollower() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID followerA = UUID.randomUUID();
        UUID followerB = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, actorId);

        when(payloadParser.parse(event)).thenReturn(new PostCreatedNotificationContext(
                actorId,
                actorId,
                "post-1",
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
        assertTrue(captor.getAllValues().stream().allMatch(cmd -> "post-1".equals(cmd.referenceId())));
    }

    private NotificationEvent sampleEvent(UUID eventId, UUID actorId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "POST_CREATED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-1",
                actorId,
                null,
                """
                        {
                          "actor_id":"%s",
                          "post_id":"post-1",
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
