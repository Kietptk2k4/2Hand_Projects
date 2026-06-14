package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.OrderCancelPendingRefundNotificationEventHandler;
import com.twohands.notification_service.application.handler.OrderCancelPendingRefundNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.OrderCancelPendingRefundNotificationContext;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancelPendingRefundNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final String ORDER_ID = UUID.randomUUID().toString();

    @Mock
    private OrderCancelPendingRefundNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private OrderCancelPendingRefundNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrderCancelPendingRefundNotificationEventHandler(
                new NotificationEventTypeAliasResolver(),
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_orderCancelPendingRefundEventTypes() {
        assertTrue(handler.supports("ORDER_CANCEL_PENDING_REFUND"));
        assertTrue(handler.supports("COMMERCE_ORDER_CANCEL_PENDING_REFUND"));
        assertFalse(handler.supports("SHIPMENT_CANCELLED"));
    }

    @Test
    void handle_notifiesBuyerWhenBuyerRequestedPendingRefund() {
        UUID sellerId = UUID.randomUUID();
        when(payloadParser.parse(any())).thenReturn(new OrderCancelPendingRefundNotificationContext(
                BUYER_ID,
                ORDER_ID,
                "refund-1",
                List.of(sellerId),
                "Changed mind",
                "BUYER",
                BUYER_ID
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase, times(2)).execute(any(CreateInAppNotificationCommand.class));
    }

    @Test
    void handle_notifiesBuyerWhenSellerRequestedPendingRefund() {
        UUID sellerId = UUID.randomUUID();
        when(payloadParser.parse(any())).thenReturn(new OrderCancelPendingRefundNotificationContext(
                BUYER_ID,
                ORDER_ID,
                "refund-1",
                List.of(sellerId),
                "Out of stock",
                "SELLER",
                sellerId
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(any(CreateInAppNotificationCommand.class));
        verify(sendPushNotificationUseCase).execute(any(SendPushNotificationCommand.class));
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
                "ORDER_CANCEL_PENDING_REFUND",
                NotificationSourceService.COMMERCE,
                "ORDER",
                ORDER_ID,
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
