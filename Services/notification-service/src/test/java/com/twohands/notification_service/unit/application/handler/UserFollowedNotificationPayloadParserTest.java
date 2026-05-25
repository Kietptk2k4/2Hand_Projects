package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.UserFollowedNotificationPayloadParser;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.UserFollowedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserFollowedNotificationPayloadParserTest {

    private UserFollowedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new UserFollowedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesFieldsFromPayload() {
        UUID actorId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();

        UserFollowedNotificationContext context = parser.parse(event(
                actorId,
                null,
                """
                        {
                          "actor_id": "%s",
                          "followed_user_id": "%s"
                        }
                        """.formatted(actorId, followedUserId)
        ));

        assertEquals(actorId, context.actorId());
        assertEquals(followedUserId, context.followedUserId());
    }

    @Test
    void parse_fallsBackToEventColumns() {
        UUID actorId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();

        UserFollowedNotificationContext context = parser.parse(event(actorId, followedUserId, "{}"));

        assertEquals(actorId, context.actorId());
        assertEquals(followedUserId, context.followedUserId());
    }

    @Test
    void parse_allowsMissingActorForHandlerValidation() {
        UUID followedUserId = UUID.randomUUID();

        UserFollowedNotificationContext context = parser.parse(event(
                null,
                followedUserId,
                """
                        {"followed_user_id":"%s"}
                        """.formatted(followedUserId)
        ));

        assertEquals(null, context.actorId());
        assertEquals(followedUserId, context.followedUserId());
    }

    @Test
    void parse_throwsWhenFollowedUserMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                UUID.randomUUID(),
                null,
                """
                        {"actor_id":"%s"}
                        """.formatted(UUID.randomUUID())
        )));
    }

    private NotificationEvent event(UUID actorId, UUID followedUserId, String payload) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "USER_FOLLOWED",
                NotificationSourceService.SOCIAL,
                "USER",
                actorId != null ? actorId.toString() : null,
                actorId,
                followedUserId,
                payload,
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
