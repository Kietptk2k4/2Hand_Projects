package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationCommand;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.CommentCreatedNotificationEventHandler;
import com.twohands.notification_service.application.handler.CommentCreatedNotificationPayloadParser;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationResult;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.delivery.SkipSelfNotificationOutcome;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.CommentCreatedNotificationContext;
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
class CommentCreatedNotificationEventHandlerTest {

    @Mock
    private CommentCreatedNotificationPayloadParser payloadParser;

    @Mock
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private CommentCreatedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CommentCreatedNotificationEventHandler(
                payloadParser,
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_onlyCommentCreated() {
        assertTrue(handler.supports("COMMENT_CREATED"));
        assertFalse(handler.supports("COMMENT_REPLIED"));
    }

    @Test
    void handle_createsInAppWithPostReference() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(eventId, actorId, postAuthorId);

        when(payloadParser.parse(event)).thenReturn(
                new CommentCreatedNotificationContext(actorId, postAuthorId, "post-42", "comment-7")
        );
        when(applySkipSelfNotificationUseCase.execute(
                new ApplySkipSelfNotificationCommand("COMMENT_CREATED", NotificationSourceService.SOCIAL, actorId, postAuthorId)
        )).thenReturn(SkipSelfNotificationOutcome.PROCEED);
        when(applyNotificationDeliveryRulesUseCase.execute(
                new ApplyNotificationDeliveryRulesCommand(postAuthorId, "COMMENT_CREATED")
        )).thenReturn(new NotificationDeliveryDecision(true, false, false));
        when(createInAppNotificationUseCase.execute(any(CreateInAppNotificationCommand.class)))
                .thenReturn(new CreateInAppNotificationResult(UUID.randomUUID(), false));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());

        ArgumentCaptor<CreateInAppNotificationCommand> captor =
                ArgumentCaptor.forClass(CreateInAppNotificationCommand.class);
        verify(createInAppNotificationUseCase).execute(captor.capture());
        assertEquals("POST", captor.getValue().referenceType());
        assertEquals("post-42", captor.getValue().referenceId());
        assertEquals(postAuthorId, captor.getValue().userId());
    }

    @Test
    void handle_returnsNoOpWhenPostAuthorCommentsOnOwnPost() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent(UUID.randomUUID(), userId, userId);

        when(payloadParser.parse(event)).thenReturn(
                new CommentCreatedNotificationContext(userId, userId, "post-1", "comment-1")
        );
        when(applySkipSelfNotificationUseCase.execute(any(ApplySkipSelfNotificationCommand.class)))
                .thenReturn(SkipSelfNotificationOutcome.SKIP);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
        verify(createInAppNotificationUseCase, never()).execute(any());
    }

    @Test
    void handle_returnsPermanentFailureWhenRequiredFieldMissing() {
        NotificationEvent event = sampleEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        when(payloadParser.parse(event))
                .thenThrow(new IllegalArgumentException("comment_id is required for COMMENT_CREATED notification event."));

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent(UUID eventId, UUID actorId, UUID postAuthorId) {
        return new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "COMMENT_CREATED",
                NotificationSourceService.SOCIAL,
                "COMMENT",
                "comment-1",
                actorId,
                postAuthorId,
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
