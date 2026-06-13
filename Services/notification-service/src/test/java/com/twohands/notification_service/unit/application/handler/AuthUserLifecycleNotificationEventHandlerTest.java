package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.handler.AuthUserLifecycleNotificationEventHandler;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUserLifecycleNotificationEventHandlerTest {

    private AuthUserLifecycleNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AuthUserLifecycleNotificationEventHandler();
    }

    @Test
    void supports_userUpdatedAndDeletedEvents() {
        assertTrue(handler.supports("USER_UPDATED"));
        assertTrue(handler.supports("USER_DELETED"));
        assertFalse(handler.supports("USER_CREATED"));
        assertFalse(handler.supports("USER_SUSPENDED"));
    }

    @Test
    void handle_completesWithoutUserFacingNotification() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent("USER_UPDATED", userId);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
    }

    @Test
    void handle_userDeleted_isNoOp() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = sampleEvent("USER_DELETED", userId);

        NotificationEventHandlerResult result = handler.handle(event);

        assertEquals(HandlerOutcome.NO_OP, result.outcome());
    }

    private static NotificationEvent sampleEvent(String eventType, UUID userId) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                eventType,
                NotificationSourceService.AUTH,
                null,
                null,
                null,
                userId,
                "{\"user_id\":\"" + userId + "\"}",
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        );
    }
}
