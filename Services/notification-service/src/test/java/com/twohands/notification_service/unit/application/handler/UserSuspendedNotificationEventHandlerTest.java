package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesCommand;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.UserSuspendedNotificationEventHandler;
import com.twohands.notification_service.application.handler.UserSuspendedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationCommand;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationCommand;
import com.twohands.notification_service.application.push.SendPushNotificationResult;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.UserSuspendedNotificationContext;
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
class UserSuspendedNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID TARGET_USER_ID = UUID.randomUUID();

    @Mock
    private UserSuspendedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private UserSuspendedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserSuspendedNotificationEventHandler(
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_accountEnforcementSuspendAndBanEvents() {
        assertTrue(handler.supports("USER_SUSPENDED"));
        assertTrue(handler.supports("USER_BANNED"));
        assertFalse(handler.supports("USER_RESTRICTED"));
    }

    @Test
    void handle_notifiesTargetUserInAppAndPush() {
        when(payloadParser.parse(any())).thenReturn(new UserSuspendedNotificationContext(
                TARGET_USER_ID,
                "enforcement-1",
                "Policy violation",
                "2026-12-31T00:00:00Z",
                "USER_ENFORCEMENT",
                "enforcement-1"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, true));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                TARGET_USER_ID,
                null,
                "USER_SUSPENDED",
                "USER_ENFORCEMENT",
                "enforcement-1",
                "{}",
                null
        ));
        verify(sendPushNotificationUseCase).execute(new SendPushNotificationCommand(
                TARGET_USER_ID,
                "USER_SUSPENDED",
                "USER_ENFORCEMENT",
                "enforcement-1",
                EVENT_ID,
                null
        ));
    }

    @Test
    void handle_notifiesTargetUserForBannedEvent() {
        when(payloadParser.parse(any())).thenReturn(new UserSuspendedNotificationContext(
                TARGET_USER_ID,
                "enforcement-2",
                "Fraud",
                null,
                "USER_ENFORCEMENT",
                "enforcement-2"
        ));
        when(applyNotificationDeliveryRulesUseCase.execute(any(ApplyNotificationDeliveryRulesCommand.class)))
                .thenReturn(new NotificationDeliveryDecision(true, true, true));
        when(sendPushNotificationUseCase.execute(any(SendPushNotificationCommand.class)))
                .thenReturn(SendPushNotificationResult.sent(1, 0));

        var result = handler.handle(sampleEvent("USER_BANNED"));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(createInAppNotificationUseCase).execute(new CreateInAppNotificationCommand(
                EVENT_ID,
                TARGET_USER_ID,
                null,
                "USER_BANNED",
                "USER_ENFORCEMENT",
                "enforcement-2",
                "{}",
                null
        ));
    }

    @Test
    void handle_returnsFailureWhenTargetUserMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("target_user_id is required"));

        var result = handler.handle(sampleEvent("USER_SUSPENDED"));

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private NotificationEvent sampleEvent() {
        return sampleEvent("USER_SUSPENDED");
    }

    private NotificationEvent sampleEvent(String eventType) {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.ADMIN,
                "USER_ENFORCEMENT",
                "enforcement-1",
                null,
                TARGET_USER_ID,
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
