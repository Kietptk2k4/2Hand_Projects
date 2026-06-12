package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.ProductRemovedNotificationEventHandler;
import com.twohands.notification_service.application.handler.ProductRemovedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.ProductRemovedNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
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
class ProductRemovedNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    @Mock
    private ProductRemovedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private ProductRemovedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ProductRemovedNotificationEventHandler(
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_productModerationEvents() {
        assertTrue(handler.supports("PRODUCT_REMOVED"));
        assertTrue(handler.supports("PRODUCT_RESTORED"));
        assertFalse(handler.supports("POST_MODERATED"));
    }

    @Test
    void handle_notifiesSellerInAppAndPush() {
        when(payloadParser.parse(any())).thenReturn(new ProductRemovedNotificationContext(
                SELLER_ID,
                "product-1",
                "Policy violation",
                "PRODUCT",
                "product-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                SELLER_ID,
                null,
                "PRODUCT_REMOVED",
                "PRODUCT",
                "product-1",
                "{}",
                null
        ));
    }

    @Test
    void handle_returnsFailureWhenSellerMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("seller_user_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    @Test
    void handle_notifiesSellerOnRestore() {
        when(payloadParser.parse(any())).thenReturn(new ProductRemovedNotificationContext(
                SELLER_ID,
                "product-1",
                "Appeal approved",
                "PRODUCT",
                "product-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(restoredEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                SELLER_ID,
                null,
                "PRODUCT_RESTORED",
                "PRODUCT",
                "product-1",
                "{}",
                null
        ));
    }

    private NotificationEvent restoredEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "PRODUCT_RESTORED",
                NotificationSourceService.ADMIN,
                "PRODUCT",
                "product-1",
                null,
                SELLER_ID,
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

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "PRODUCT_REMOVED",
                NotificationSourceService.ADMIN,
                "PRODUCT",
                "product-1",
                null,
                SELLER_ID,
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
