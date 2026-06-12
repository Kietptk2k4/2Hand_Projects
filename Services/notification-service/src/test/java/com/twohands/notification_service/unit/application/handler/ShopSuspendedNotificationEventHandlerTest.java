package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.ShopSuspendedNotificationEventHandler;
import com.twohands.notification_service.application.handler.ShopSuspendedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.ShopSuspendedNotificationContext;
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
class ShopSuspendedNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID SHOP_OWNER_ID = UUID.randomUUID();

    @Mock
    private ShopSuspendedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private ShopSuspendedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ShopSuspendedNotificationEventHandler(
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_shopModerationEvents() {
        assertTrue(handler.supports("SHOP_SUSPENDED"));
        assertTrue(handler.supports("SHOP_RESTORED"));
        assertFalse(handler.supports("USER_SUSPENDED"));
    }

    @Test
    void handle_notifiesShopOwnerForRestored() {
        when(payloadParser.parse(any())).thenReturn(new ShopSuspendedNotificationContext(
                SHOP_OWNER_ID,
                "shop-1",
                "Appeal approved",
                null,
                "SHOP",
                "shop-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, false));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent("SHOP_RESTORED"));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                SHOP_OWNER_ID,
                null,
                "SHOP_RESTORED",
                "SHOP",
                "shop-1",
                "{}",
                null
        ));
    }

    @Test
    void handle_notifiesShopOwnerInAppAndPush() {
        when(payloadParser.parse(any())).thenReturn(new ShopSuspendedNotificationContext(
                SHOP_OWNER_ID,
                "shop-1",
                "Policy violation",
                "2026-12-31T00:00:00Z",
                "SHOP",
                "shop-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, true));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                SHOP_OWNER_ID,
                null,
                "SHOP_SUSPENDED",
                "SHOP",
                "shop-1",
                "{}",
                null
        ));
    }

    private NotificationEvent sampleEvent() {
        return sampleEvent("SHOP_SUSPENDED");
    }

    private NotificationEvent sampleEvent(String eventType) {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.ADMIN,
                "SHOP",
                "shop-1",
                null,
                SHOP_OWNER_ID,
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
