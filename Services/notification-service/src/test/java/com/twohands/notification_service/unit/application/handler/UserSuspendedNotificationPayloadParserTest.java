package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.UserSuspendedNotificationPayloadParser;
import com.twohands.notification_service.domain.admin.UserSuspendedNotificationContext;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserSuspendedNotificationPayloadParserTest {

    private static final UUID TARGET_USER_ID = UUID.randomUUID();
    private static final String ENFORCEMENT_ID = "enforcement-1";

    private UserSuspendedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new UserSuspendedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesTargetUserAndEnforcementReference() {
        UserSuspendedNotificationContext context = parser.parse(event(
                """
                        {
                          "user_id":"%s",
                          "enforcement_id":"%s",
                          "enforcement_reason":"Repeated policy violations",
                          "enforcement_expires_at":"2026-12-31T00:00:00Z"
                        }
                        """.formatted(TARGET_USER_ID, ENFORCEMENT_ID)
        ));

        assertEquals(TARGET_USER_ID, context.targetUserId());
        assertEquals(ENFORCEMENT_ID, context.enforcementId());
        assertEquals("Repeated policy violations", context.enforcementReason());
        assertEquals("2026-12-31T00:00:00Z", context.enforcementExpiresAt());
        assertEquals("USER_ENFORCEMENT", context.referenceType());
        assertEquals(ENFORCEMENT_ID, context.referenceId());
    }

    @Test
    void parse_resolvesReasonFromReasonCodeWhenDescriptionUnsafe() {
        UserSuspendedNotificationContext context = parser.parse(event(
                """
                        {
                          "target_user_id":"%s",
                          "enforcement_id":"%s",
                          "description":"internal admin note",
                          "reason_code":"SPAM_ABUSE"
                        }
                        """.formatted(TARGET_USER_ID, ENFORCEMENT_ID)
        ));

        assertEquals("Spam abuse", context.enforcementReason());
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
                "USER_SUSPENDED",
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
                "USER_SUSPENDED",
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
                "USER_SUSPENDED",
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
