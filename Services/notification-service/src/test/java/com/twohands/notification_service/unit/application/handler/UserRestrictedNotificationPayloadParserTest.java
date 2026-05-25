package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.UserRestrictedNotificationPayloadParser;
import com.twohands.notification_service.domain.admin.UserRestrictedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRestrictedNotificationPayloadParserTest {

    private static final UUID TARGET_USER_ID = UUID.randomUUID();
    private static final String ENFORCEMENT_ID = "enforcement-1";

    private UserRestrictedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new UserRestrictedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesTargetUserEnforcementAndCapabilitiesSummary() {
        UserRestrictedNotificationContext context = parser.parse(event(
                """
                        {
                          "user_id":"%s",
                          "enforcement_id":"%s",
                          "enforcement_reason":"Policy violation",
                          "restricted_capabilities_summary":"Creating posts, Commenting",
                          "enforcement_expires_at":"2026-12-31T00:00:00Z"
                        }
                        """.formatted(TARGET_USER_ID, ENFORCEMENT_ID)
        ));

        assertEquals(TARGET_USER_ID, context.targetUserId());
        assertEquals(ENFORCEMENT_ID, context.enforcementId());
        assertEquals("Policy violation", context.enforcementReason());
        assertEquals("Creating posts, Commenting", context.restrictedCapabilitiesSummary());
        assertEquals("USER_ENFORCEMENT", context.referenceType());
    }

    @Test
    void parse_throwsWhenTargetUserMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutTargetUser(
                """
                        {"enforcement_id":"%s"}
                        """.formatted(ENFORCEMENT_ID)
        )));
    }

    @Test
    void parse_throwsWhenEnforcementIdMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(eventWithoutEnforcementAggregate(
                """
                        {"target_user_id":"%s"}
                        """.formatted(TARGET_USER_ID)
        )));
    }

    private NotificationEvent event(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "USER_RESTRICTED",
                NotificationSourceService.ADMIN,
                "USER_ENFORCEMENT",
                ENFORCEMENT_ID,
                null,
                TARGET_USER_ID,
                payload,
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

    private NotificationEvent eventWithoutTargetUser(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "USER_RESTRICTED",
                NotificationSourceService.ADMIN,
                "USER_ENFORCEMENT",
                ENFORCEMENT_ID,
                null,
                null,
                payload,
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

    private NotificationEvent eventWithoutEnforcementAggregate(String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "USER_RESTRICTED",
                NotificationSourceService.ADMIN,
                null,
                null,
                null,
                null,
                payload,
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
