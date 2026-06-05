package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.handler.UserEnforcementLiftedNotificationEventHandler;
import com.twohands.notification_service.application.handler.UserEnforcementLiftedNotificationPayloadParser;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import com.twohands.notification_service.application.push.SendPushNotificationUseCase;
import com.twohands.notification_service.domain.admin.UserEnforcementLiftedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserEnforcementLiftedNotificationEventHandlerTest {

    @Mock
    private UserEnforcementLiftedNotificationPayloadParser payloadParser;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    @Mock
    private SendPushNotificationUseCase sendPushNotificationUseCase;

    private UserEnforcementLiftedNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UserEnforcementLiftedNotificationEventHandler(
                payloadParser,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase,
                sendPushNotificationUseCase
        );
    }

    @Test
    void supports_revokedAndExpired() {
        assertTrue(handler.supports("USER_ENFORCEMENT_REVOKED"));
        assertTrue(handler.supports("USER_ENFORCEMENT_EXPIRED"));
        assertFalse(handler.supports("USER_SUSPENDED"));
    }
}
