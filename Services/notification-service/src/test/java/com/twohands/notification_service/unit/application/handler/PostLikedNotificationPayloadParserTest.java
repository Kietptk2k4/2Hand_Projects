package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.PostLikedNotificationPayloadParser;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.PostLikedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostLikedNotificationPayloadParserTest {

    private PostLikedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new PostLikedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesFieldsFromPayload() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        PostLikedNotificationContext context = parser.parse(event(
                actorId,
                null,
                "POST",
                "post-42",
                """
                        {
                          "actor_id": "%s",
                          "post_author_id": "%s",
                          "post_id": "post-42"
                        }
                        """.formatted(actorId, postAuthorId)
        ));

        assertEquals(actorId, context.actorId());
        assertEquals(postAuthorId, context.postAuthorId());
        assertEquals("post-42", context.postId());
    }

    @Test
    void parse_fallsBackToEventColumns() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        PostLikedNotificationContext context = parser.parse(event(
                actorId,
                postAuthorId,
                "POST",
                "post-from-aggregate",
                "{}"
        ));

        assertEquals(actorId, context.actorId());
        assertEquals(postAuthorId, context.postAuthorId());
        assertEquals("post-from-aggregate", context.postId());
    }

    @Test
    void parse_throwsWhenPostAuthorMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                UUID.randomUUID(),
                null,
                "POST",
                "post-1",
                """
                        {"actor_id":"%s","post_id":"post-1"}
                        """.formatted(UUID.randomUUID())
        )));
    }

    @Test
    void parse_allowsMissingActorForHandlerValidation() {
        UUID postAuthorId = UUID.randomUUID();

        PostLikedNotificationContext context = parser.parse(event(
                null,
                postAuthorId,
                "POST",
                "post-1",
                """
                        {"post_author_id":"%s","post_id":"post-1"}
                        """.formatted(postAuthorId)
        ));

        assertEquals(null, context.actorId());
        assertEquals(postAuthorId, context.postAuthorId());
    }

    @Test
    void parse_throwsWhenPostIdMissing() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                actorId,
                postAuthorId,
                null,
                null,
                """
                        {"actor_id":"%s","post_author_id":"%s"}
                        """.formatted(actorId, postAuthorId)
        )));
    }

    private NotificationEvent event(
            UUID actorId,
            UUID recipientId,
            String aggregateType,
            String aggregateId,
            String payload
    ) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                aggregateType,
                aggregateId,
                actorId,
                recipientId,
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
