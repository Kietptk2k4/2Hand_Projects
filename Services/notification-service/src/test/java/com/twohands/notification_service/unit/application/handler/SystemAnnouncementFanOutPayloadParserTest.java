package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.SystemAnnouncementFanOutPayloadParser;
import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemAnnouncementFanOutPayloadParserTest {

    private SystemAnnouncementFanOutPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new SystemAnnouncementFanOutPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesAnnouncementFieldsAndRecipients() {
        UUID recipientOne = UUID.randomUUID();
        UUID recipientTwo = UUID.randomUUID();
        UUID announcementId = UUID.randomUUID();

        SystemAnnouncementFanOutContext context = parser.parse(sampleEvent(
                "ANNOUNCEMENT",
                announcementId.toString(),
                """
                        {
                          "announcement_id":"%s",
                          "title":"Maintenance",
                          "content":"Scheduled downtime tonight.",
                          "severity":"WARNING",
                          "is_pinned":true,
                          "dismissible":true,
                          "recipient_user_ids":["%s","%s"]
                        }
                        """.formatted(announcementId, recipientOne, recipientTwo)
        ));

        assertEquals(announcementId.toString(), context.announcementId());
        assertEquals("Maintenance", context.title());
        assertEquals("Scheduled downtime tonight.", context.content());
        assertEquals("WARNING", context.severity());
        assertTrue(context.isPinned());
        assertTrue(context.dismissible());
        assertEquals(List.of(recipientOne, recipientTwo), context.explicitRecipientUserIds());
        assertEquals("SYSTEM_ANNOUNCEMENT", context.referenceType());
        assertEquals(announcementId.toString(), context.referenceId());
    }

    @Test
    void parse_throwsWhenAnnouncementIdMissing() {
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(sampleEvent("OTHER", "ignored", """
                        {
                          "title":"T",
                          "content":"C",
                          "severity":"INFO"
                        }
                        """))
        );
    }

    @Test
    void parse_defaultsIsPinnedFalseWhenMissing() {
        UUID announcementId = UUID.randomUUID();

        SystemAnnouncementFanOutContext context = parser.parse(sampleEvent(
                "ANNOUNCEMENT",
                announcementId.toString(),
                """
                        {
                          "announcement_id":"%s",
                          "title":"Info",
                          "content":"Body",
                          "severity":"INFO",
                          "recipient_user_ids":["%s"]
                        }
                        """.formatted(announcementId, UUID.randomUUID())
        ));

        assertFalse(context.isPinned());
    }

    @Test
    void parse_throwsWhenIsPinnedShapeInvalid() {
        UUID announcementId = UUID.randomUUID();
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(sampleEvent("ANNOUNCEMENT", announcementId.toString(), """
                        {
                          "announcement_id":"%s",
                          "title":"T",
                          "content":"C",
                          "severity":"INFO",
                          "is_pinned":{"invalid":true}
                        }
                        """.formatted(announcementId)))
        );
    }

    @Test
    void parse_throwsWhenTitleMissing() {
        UUID announcementId = UUID.randomUUID();
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(sampleEvent("ANNOUNCEMENT", announcementId.toString(), """
                        {
                          "announcement_id":"%s",
                          "content":"C",
                          "severity":"INFO"
                        }
                        """.formatted(announcementId)))
        );
    }

    private NotificationEvent sampleEvent(String aggregateType, String aggregateId, String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SYSTEM_ANNOUNCEMENT_SENT",
                NotificationSourceService.ADMIN,
                aggregateType,
                aggregateId,
                null,
                null,
                payload,
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
