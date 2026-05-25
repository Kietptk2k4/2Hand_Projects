package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationDeliveryChannelPolicy;
import com.twohands.notification_service.application.handler.NotificationRecipientResolver;
import com.twohands.notification_service.application.handler.PushNotificationEventHandler;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationOutcome;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    @Mock
    private NotificationRecipientResolver recipientResolver;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private PushNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PushNotificationEventHandler(
                new NotificationDeliveryChannelPolicy(),
                recipientResolver,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_pushOnlyEligibleEvents() {
        assertFalse(handler.supports("PAYMENT_FAILED"));
        assertFalse(handler.supports("COMMERCE_PAYMENT_FAILED"));
        assertFalse(handler.supports("SHIPMENT_SHIPPED"));
        assertFalse(handler.supports("COMMERCE_SHIPMENT_SHIPPED"));
        assertFalse(handler.supports("SHIPMENT_DELIVERED"));
        assertFalse(handler.supports("COMMERCE_SHIPMENT_DELIVERED"));
        assertFalse(handler.supports("ORDER_COMPLETED"));
        assertFalse(handler.supports("COMMERCE_ORDER_COMPLETED"));
        assertFalse(handler.supports("USER_SUSPENDED"));
        assertFalse(handler.supports("USER_RESTRICTED"));
        assertTrue(handler.supports("PRODUCT_REMOVED"));
        assertFalse(handler.supports("ORDER_CREATED"));
        assertFalse(handler.supports("COMMERCE_ORDER_CREATED"));
        assertFalse(handler.supports("PAYMENT_SUCCESS"));
        assertFalse(handler.supports("COMMERCE_PAYMENT_PAID"));
        assertFalse(handler.supports("POST_LIKED"));
        assertFalse(handler.supports("USER_CREATED"));
    }

    @Test
    void handle_returnsNoOpWhenPushSkipped() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.skipped("No active device tokens."));

        var result = handler.handle(sampleEvent("PRODUCT_REMOVED"));

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
    }

    @Test
    void handle_returnsFailureWhenPushFailsRetryable() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.failed(
                        NotificationFailurePolicy.RETRYABLE,
                        "FCM provider timeout.",
                        0
                ));

        var result = handler.handle(sampleEvent("PRODUCT_REMOVED"));

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.RETRYABLE, result.failurePolicy());
    }

    @Test
    void handle_returnsSuccessWhenPushSent() {
        when(recipientResolver.resolve(any())).thenReturn(List.of(RECIPIENT_ID));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent("PRODUCT_REMOVED"));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
    }

    private NotificationEvent sampleEvent(String eventType) {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.COMMERCE,
                "REVIEW",
                "review-1",
                null,
                RECIPIENT_ID,
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
