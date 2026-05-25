package com.twohands.notification_service.unit.application.handler;

import com.twohands.notification_service.application.announcement.FanOutSystemAnnouncementCommand;
import com.twohands.notification_service.application.announcement.FanOutSystemAnnouncementResult;
import com.twohands.notification_service.application.announcement.FanOutSystemAnnouncementUseCase;
import com.twohands.notification_service.application.announcement.ResolveSystemAnnouncementRecipientsUseCase;
import com.twohands.notification_service.application.handler.HandlerOutcome;
import com.twohands.notification_service.application.handler.SystemAnnouncementFanOutNotificationEventHandler;
import com.twohands.notification_service.application.handler.SystemAnnouncementFanOutPayloadParser;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.announcement.SystemAnnouncementAudienceUserProvider;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemAnnouncementFanOutNotificationEventHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();

    @Mock
    private SystemAnnouncementFanOutPayloadParser payloadParser;

    @Mock
    private ResolveSystemAnnouncementRecipientsUseCase resolveSystemAnnouncementRecipientsUseCase;

    @Mock
    private FanOutSystemAnnouncementUseCase fanOutSystemAnnouncementUseCase;

    @Mock
    private SystemAnnouncementAudienceUserProvider audienceUserProvider;

    private SystemAnnouncementFanOutNotificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SystemAnnouncementFanOutNotificationEventHandler(
                payloadParser,
                resolveSystemAnnouncementRecipientsUseCase,
                fanOutSystemAnnouncementUseCase,
                audienceUserProvider
        );
    }

    @Test
    void supports_systemAnnouncementSentOnly() {
        assertTrue(handler.supports("SYSTEM_ANNOUNCEMENT_SENT"));
        assertFalse(handler.supports("SHOP_SUSPENDED"));
    }

    @Test
    void handle_fansOutToResolvedRecipients() {
        SystemAnnouncementFanOutContext context = sampleContext();
        when(payloadParser.parse(any())).thenReturn(context);
        when(resolveSystemAnnouncementRecipientsUseCase.execute(context)).thenReturn(List.of(RECIPIENT_ID));
        when(fanOutSystemAnnouncementUseCase.execute(any(FanOutSystemAnnouncementCommand.class)))
                .thenReturn(new FanOutSystemAnnouncementResult(1, 0, true, null));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.SUCCESS, result.outcome());
        verify(fanOutSystemAnnouncementUseCase).execute(new FanOutSystemAnnouncementCommand(
                EVENT_ID,
                context,
                List.of(RECIPIENT_ID)
        ));
    }

    @Test
    void handle_returnsPermanentFailureWhenAnnouncementIdMissing() {
        when(payloadParser.parse(any())).thenThrow(new IllegalArgumentException("announcement_id is required"));

        var result = handler.handle(sampleEvent());

        assertEquals(HandlerOutcome.FAILURE, result.outcome());
        assertEquals(NotificationFailurePolicy.PERMANENT, result.failurePolicy());
    }

    private SystemAnnouncementFanOutContext sampleContext() {
        return new SystemAnnouncementFanOutContext(
                "ann-1",
                "Title",
                "Content",
                "INFO",
                true,
                true,
                List.of(RECIPIENT_ID),
                null,
                "SYSTEM_ANNOUNCEMENT",
                "ann-1"
        );
    }

    private NotificationEvent sampleEvent() {
        return new NotificationEvent(
                EVENT_ID,
                UUID.randomUUID(),
                null,
                "SYSTEM_ANNOUNCEMENT_SENT",
                NotificationSourceService.ADMIN,
                "ANNOUNCEMENT",
                "ann-1",
                null,
                null,
                "{}",
                NotificationEventStatus.PENDING,
                0,
                5,
                null,
                null,
                null,
                Instant.now(),
                null
        );
    }
}
