package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.announcement.WithdrawSystemAnnouncementNotificationsUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.SystemAnnouncementCancelledNotificationEventHandler;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemAnnouncementCancelledNotificationEventHandlerTest {

    private static final String ANNOUNCEMENT_ID = "a1111111-1111-1111-1111-111111111111";

    @Mock
    private WithdrawSystemAnnouncementNotificationsUseCase withdrawSystemAnnouncementNotificationsUseCase;

    private SystemAnnouncementCancelledNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SystemAnnouncementCancelledNotificationEventHandler(
                new ObjectMapper(),
                withdrawSystemAnnouncementNotificationsUseCase
        );
    }

    @Test
    void handle_withdrawsNotificationsByAnnouncementId() {
        when(withdrawSystemAnnouncementNotificationsUseCase.execute(ANNOUNCEMENT_ID)).thenReturn(3);

        var result = handler.handle(event("""
                {"announcement_id":"%s","title":"T","content":"C","severity":"INFO","status":"CANCELLED"}
                """.formatted(ANNOUNCEMENT_ID)));

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(withdrawSystemAnnouncementNotificationsUseCase).execute(eq(ANNOUNCEMENT_ID));
    }

    @Test
    void supports_cancelledOnly() {
        assertTrue(handler.supports("SYSTEM_ANNOUNCEMENT_CANCELLED"));
    }

    private NotificationEvent event(String payload) {
        Instant now = Instant.now();
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "admin.announcement.cancelled:" + ANNOUNCEMENT_ID,
                "SYSTEM_ANNOUNCEMENT_CANCELLED",
                NotificationSourceService.ADMIN,
                "SYSTEM_ANNOUNCEMENT",
                ANNOUNCEMENT_ID,
                null,
                null,
                payload,
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                now,
                null
        );
    }
}
