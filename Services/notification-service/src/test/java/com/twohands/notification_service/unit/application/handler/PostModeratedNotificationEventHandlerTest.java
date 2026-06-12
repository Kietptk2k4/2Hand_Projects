package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.PostModeratedNotificationEventHandler;
import com.twohands.notification_service.application.handler.PostModeratedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.PostModeratedNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostModeratedNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID AUTHOR_ID = UUID.randomUUID();
    private static final String POST_ID = "507f1f77bcf86cd799439011";

    @Mock
    private PostModeratedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private PostModeratedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PostModeratedNotificationEventHandler(
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_postModeratedOnly() {
        assertTrue(handler.supports("POST_MODERATED"));
        assertFalse(handler.supports("PRODUCT_REMOVED"));
    }

    @Test
    void handle_notifiesAuthorInAppAndPush() {
        when(payloadParser.parse(any())).thenReturn(new PostModeratedNotificationContext(
                AUTHOR_ID,
                POST_ID,
                "HIDE",
                "Policy violation",
                "POST",
                POST_ID,
                InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                AUTHOR_ID,
                null,
                "POST_MODERATED",
                "POST",
                POST_ID,
                "{}",
                InAppNotificationTemplatePolicy.HIDE_TEMPLATE_VARIANT
        ));
    }

    @Test
    void handle_returnsFailureWhenAuthorMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("author_user_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "POST_MODERATED",
                NotificationSourceService.ADMIN,
                "POST",
                POST_ID,
                null,
                AUTHOR_ID,
                "{}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker",
                Instant.now(),
                null
        );
    }
}
