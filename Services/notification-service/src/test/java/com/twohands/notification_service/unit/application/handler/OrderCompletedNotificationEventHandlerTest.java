package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.OrderCompletedNotificationEventHandler;
import com.twohands.notification_service.application.handler.OrderCompletedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.OrderCompletedNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
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
class OrderCompletedNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();

    @Mock
    private OrderCompletedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private OrderCompletedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderCompletedNotificationEventHandler(
                new NotificationEventTypeAliasResolver(),
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_orderCompletedEventTypes() {
        assertTrue(handler.supports("ORDER_COMPLETED"));
        assertTrue(handler.supports("COMMERCE_ORDER_COMPLETED"));
        assertFalse(handler.supports("ORDER_CREATED"));
    }

    @Test
    void handle_notifiesBuyerInAppAndPush() {
        when(payloadParser.parse(any())).thenReturn(new OrderCompletedNotificationContext(
                BUYER_ID,
                "order-1",
                "ORD-1",
                "2026-05-25T12:00:00Z"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                BUYER_ID,
                null,
                "ORDER_COMPLETED",
                "ORDER",
                "order-1",
                "{}",
                null
        ));
        verify(sendPushNotificationUseCase).execute(new SendPushNotificationCommand(
                BUYER_ID,
                "ORDER_COMPLETED",
                "ORDER",
                "order-1",
                EVENT_ID,
                null
        ));
    }

    @Test
    void handle_returnsFailureWhenBuyerIdMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("buyer_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "ORDER_COMPLETED",
                NotificationSourceService.COMMERCE,
                "ORDER",
                "order-1",
                null,
                BUYER_ID,
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
