package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.PayoutRequestApprovedNotificationEventHandler;
import com.twohands.notification_service.application.handler.PayoutRequestApprovedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.domain.commerce.PayoutRequestApprovedNotificationContext;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutRequestApprovedNotificationEventHandlerTest {

    @Mock
    private PayoutRequestApprovedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private PayoutRequestApprovedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PayoutRequestApprovedNotificationEventHandler(
                new NotificationEventTypeAliasResolver(),
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_payoutApprovedEventTypes() {
        assertTrue(handler.supports("PAYOUT_REQUEST_APPROVED"));
        assertTrue(handler.supports("COMMERCE_PAYOUT_REQUEST_APPROVED"));
    }

    @Test
    void handle_notifiesSellerInApp() {
        UUID sellerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(payloadParser.parse(any())).thenReturn(new PayoutRequestApprovedNotificationContext(
                sellerId,
                "payout-1",
                new BigDecimal("150000"),
                "2026-06-04T14:00:00Z"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, false, false));

        var result = handler.handle(new NotificationEvent(
                eventId,
                UUID.randomUUID(),
                null,
                "PAYOUT_REQUEST_APPROVED",
                NotificationSourceService.COMMERCE,
                "PAYOUT_REQUEST",
                "payout-1",
                null,
                sellerId,
                "{\"seller_id\":\"" + sellerId + "\",\"payout_request_id\":\"payout-1\"}",
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        ));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(any());
    }
}
