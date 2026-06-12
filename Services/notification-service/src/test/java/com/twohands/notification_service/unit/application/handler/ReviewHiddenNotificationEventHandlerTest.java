package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.ReviewHiddenNotificationEventHandler;
import com.twohands.notification_service.application.handler.ReviewHiddenNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.ReviewHiddenNotificationContext;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewHiddenNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID AUTHOR_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    @Mock
    private ReviewHiddenNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private ReviewHiddenNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReviewHiddenNotificationEventHandler(
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_reviewModerationEvents() {
        assertTrue(handler.supports("REVIEW_HIDDEN"));
        assertTrue(handler.supports("REVIEW_REMOVED"));
        assertTrue(handler.supports("REVIEW_RESTORED"));
        assertFalse(handler.supports("PRODUCT_REMOVED"));
    }

    @Test
    void handle_notifiesAuthorAndSellerInAppOnly() {
        when(payloadParser.parse(any())).thenReturn(new ReviewHiddenNotificationContext(
                AUTHOR_ID,
                SELLER_ID,
                "review-1",
                "Policy violation",
                "REVIEW",
                "review-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, false, false));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                AUTHOR_ID,
                null,
                "REVIEW_HIDDEN",
                "REVIEW",
                "review-1",
                "{}",
                null
        ));
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                SELLER_ID,
                null,
                "REVIEW_HIDDEN",
                "REVIEW",
                "review-1",
                "{}",
                InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT
        ));
    }

    @Test
    void handle_returnsFailureWhenNoRecipientResolvable() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("review_author_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    @Test
    void handle_notifiesAuthorAndSellerInAppAndPushForRemoved() {
        when(payloadParser.parse(any())).thenReturn(new ReviewHiddenNotificationContext(
                AUTHOR_ID,
                SELLER_ID,
                "review-1",
                "Policy violation",
                "REVIEW",
                "review-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent("REVIEW_REMOVED"));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase, times(2)).execute(any(CreateInAppNotificationCommand.class));
        verify(sendPushNotificationUseCase, times(2)).execute(any(SendPushNotificationCommand.class));
    }

    @Test
    void handle_deduplicatesWhenAuthorIsSeller() {
        when(payloadParser.parse(any())).thenReturn(new ReviewHiddenNotificationContext(
                AUTHOR_ID,
                AUTHOR_ID,
                "review-1",
                null,
                "REVIEW",
                "review-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, false, false));

        handler.handle(sampleEvent());

        verify(createInAppNotificationUseCase, times(1)).execute(any(CreateInAppNotificationCommand.class));
        verify(createInAppNotificationUseCase).execute(eq(new CreateInAppNotificationCommand(
                EVENT_ID,
                AUTHOR_ID,
                null,
                "REVIEW_HIDDEN",
                "REVIEW",
                "review-1",
                "{}",
                null
        )));
    }

    private NotificationEvent sampleEvent() {
        return sampleEvent("REVIEW_HIDDEN");
    }

    private NotificationEvent sampleEvent(String eventType) {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.ADMIN,
                "REVIEW",
                "review-1",
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
