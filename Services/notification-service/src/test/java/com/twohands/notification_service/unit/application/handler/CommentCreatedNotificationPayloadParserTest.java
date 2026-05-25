package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.CommentCreatedNotificationPayloadParser;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.CommentCreatedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentCreatedNotificationPayloadParserTest {

    private CommentCreatedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommentCreatedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesFieldsFromPayload() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        CommentCreatedNotificationContext context = parser.parse(event(
                actorId,
                null,
                null,
                null,
                """
                        {
                          "actor_id": "%s",
                          "post_author_id": "%s",
                          "post_id": "post-1",
                          "comment_id": "comment-9"
                        }
                        """.formatted(actorId, postAuthorId)
        ));

        assertEquals(actorId, context.actorId());
        assertEquals(postAuthorId, context.postAuthorId());
        assertEquals("post-1", context.postId());
        assertEquals("comment-9", context.commentId());
    }

    @Test
    void parse_fallsBackToEventColumns() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        CommentCreatedNotificationContext context = parser.parse(event(
                actorId,
                postAuthorId,
                "POST",
                "post-agg",
                """
                        {"comment_id":"comment-1"}
                        """
        ));

        assertEquals("post-agg", context.postId());
        assertEquals("comment-1", context.commentId());
    }

    @Test
    void parse_fallsBackToCommentAggregateForCommentId() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        CommentCreatedNotificationContext context = parser.parse(new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "COMMENT_CREATED",
                NotificationSourceService.SOCIAL,
                "COMMENT",
                "comment-agg",
                actorId,
                postAuthorId,
                """
                        {"post_author_id":"%s","post_id":"post-1"}
                        """.formatted(postAuthorId),
                NotificationEventStatus.PROCESSING,
                0,
                5,
                null,
                Instant.now(),
                "worker-1",
                Instant.now(),
                null
        ));

        assertEquals("comment-agg", context.commentId());
        assertEquals("post-1", context.postId());
    }

    @Test
    void parse_throwsWhenPostAuthorMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                UUID.randomUUID(),
                null,
                "POST",
                "post-1",
                """
                        {"actor_id":"%s","post_id":"post-1","comment_id":"c-1"}
                        """.formatted(UUID.randomUUID())
        )));
    }

    @Test
    void parse_throwsWhenCommentIdMissing() {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                actorId,
                postAuthorId,
                "POST",
                "post-1",
                """
                        {"actor_id":"%s","post_author_id":"%s","post_id":"post-1"}
                        """.formatted(actorId, postAuthorId)
        )));
    }

    private NotificationEvent event(
            UUID actorId,
            UUID postAuthorId,
            String aggregateType,
            String aggregateId,
            String payload
    ) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "COMMENT_CREATED",
                NotificationSourceService.SOCIAL,
                aggregateType,
                aggregateId,
                actorId,
                postAuthorId,
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
