package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.ShipmentCreatedNotificationEventHandler;
import com.twohands.notification_service.application.handler.ShipmentCreatedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.commerce.ShipmentCreatedNotificationContext;
import com.twohands.notification_service.domain.delivery.NotificationDeliveryDecision;
import com.twohands.notification_service.domain.inapp.InAppNotificationTemplatePolicy;
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
class ShipmentCreatedNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID BUYER_ID = UUID.randomUUID();
    private static final UUID SELLER_ID = UUID.randomUUID();

    @Mock
    private ShipmentCreatedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    private ShipmentCreatedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ShipmentCreatedNotificationEventHandler(
                new NotificationEventTypeAliasResolver(),
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase
        );
    }

    @Test
    void supports_shipmentCreatedEventTypes() {
        assertTrue(handler.supports("SHIPMENT_CREATED"));
        assertTrue(handler.supports("COMMERCE_SHIPMENT_CREATED"));
        assertFalse(handler.supports("SHIPMENT_SHIPPED"));
    }

    @Test
    void handle_notifiesBuyerAndSellerInApp() {
        when(payloadParser.parse(any())).thenReturn(new ShipmentCreatedNotificationContext(
                BUYER_ID,
                SELLER_ID,
                "ship-1",
                "order-1",
                "VN123"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, false, false));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                BUYER_ID,
                BUYER_ID,
                "SHIPMENT_CREATED",
                "SHIPMENT",
                "ship-1",
                "{\"recipient_audience\":\"buyer\"}",
                null
        ));
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                SELLER_ID,
                BUYER_ID,
                "SHIPMENT_CREATED",
                "SHIPMENT",
                "ship-1",
                "{\"recipient_audience\":\"seller\"}",
                InAppNotificationTemplatePolicy.SELLER_TEMPLATE_VARIANT
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
                "SHIPMENT_CREATED",
                NotificationSourceService.COMMERCE,
                "SHIPMENT",
                "ship-1",
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
