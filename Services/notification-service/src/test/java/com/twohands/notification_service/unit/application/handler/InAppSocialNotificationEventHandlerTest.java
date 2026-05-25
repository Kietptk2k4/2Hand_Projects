package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.delivery.ApplyNotificationDeliveryRulesUseCase;
import com.twohands.notification_service.application.delivery.ApplySkipSelfNotificationUseCase;
import com.twohands.notification_service.application.handler.InAppSocialNotificationEventHandler;
import com.twohands.notification_service.application.handler.NotificationDeliveryChannelPolicy;
import com.twohands.notification_service.application.handler.NotificationRecipientResolver;
import com.twohands.notification_service.application.inapp.CreateInAppNotificationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class InAppSocialNotificationEventHandlerTest {

    @Mock
    private ApplySkipSelfNotificationUseCase applySkipSelfNotificationUseCase;

    @Mock
    private ApplyNotificationDeliveryRulesUseCase applyNotificationDeliveryRulesUseCase;

    @Mock
    private CreateInAppNotificationUseCase createInAppNotificationUseCase;

    private InAppSocialNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InAppSocialNotificationEventHandler(
                new NotificationDeliveryChannelPolicy(),
                new NotificationRecipientResolver(new ObjectMapper()),
                applySkipSelfNotificationUseCase,
                applyNotificationDeliveryRulesUseCase,
                createInAppNotificationUseCase
        );
    }

    @Test
    void supports_doesNotHandleDedicatedSocialEvents() {
        assertFalse(handler.supports("POST_LIKED"));
        assertFalse(handler.supports("USER_FOLLOWED"));
        assertFalse(handler.supports("COMMENT_CREATED"));
        assertFalse(handler.supports("COMMENT_REPLIED"));
        assertFalse(handler.supports("COMMENT_LIKED"));
    }
}
