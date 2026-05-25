package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.email.SendEmailNotificationCommand;
import com.twohands.notification_service.application.email.SendEmailNotificationOutcome;
import com.twohands.notification_service.application.email.SendEmailNotificationResult;
import com.twohands.notification_service.application.email.SendEmailNotificationUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.ShopSuspendedEmailNotificationEventHandler;
import com.twohands.notification_service.application.handler.ShopSuspendedNotificationPayloadParser;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.ShopSuspendedNotificationContext;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopSuspendedEmailNotificationEventHandlerTest {

    private static final UUID SHOP_OWNER_ID = UUID.randomUUID();

    @Mock
    private ShopSuspendedNotificationPayloadParser payloadParser;

    @Mock
    private SendEmailNotificationUseCase sendEmailNotificationUseCase;

    private ShopSuspendedEmailNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ShopSuspendedEmailNotificationEventHandler(payloadParser, sendEmailNotificationUseCase);
    }

    @Test
    void supports_shopSuspendedOnly() {
        assertTrue(handler.supports("SHOP_SUSPENDED"));
        assertFalse(handler.supports("PRODUCT_REMOVED"));
    }

    @Test
    void handle_sendsEmailToShopOwner() {
        when(payloadParser.parse(any())).thenReturn(new ShopSuspendedNotificationContext(
                SHOP_OWNER_ID,
                "shop-1",
                "Policy violation",
                null,
                "SHOP",
                "shop-1"
        ));
        when(sendEmailNotificationUseCase.execute(any(SendEmailNotificationCommand.class)))
                .thenReturn(SendEmailNotificationResult.sent("msg-1"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
    }

    @Test
    void handle_returnsFailureWhenShopOwnerMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("shop_owner_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SHOP_SUSPENDED",
                NotificationSourceService.ADMIN,
                "SHOP",
                "shop-1",
                null,
                SHOP_OWNER_ID,
                """
                        {"recipient_email":"owner@example.com"}
                        """,
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
