package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.CommentLikedNotificationPayloadParser;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.CommentLikedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentLikedNotificationPayloadParserTest {

    private CommentLikedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommentLikedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesFieldsFromPayload() {
        UUID actorId = UUID.randomUUID();
        UUID commentAuthorId = UUID.randomUUID();

        CommentLikedNotificationContext context = parser.parse(event(
                actorId,
                null,
                null,
                null,
                """
                        {
                          "actor_id": "%s",
                          "comment_author_id": "%s",
                          "comment_id": "comment-liked-9"
                        }
                        """.formatted(actorId, commentAuthorId)
        ));

        assertEquals(actorId, context.actorId());
        assertEquals(commentAuthorId, context.commentAuthorId());
        assertEquals("comment-liked-9", context.commentId());
    }

    @Test
    void parse_fallsBackToEventColumns() {
        UUID actorId = UUID.randomUUID();
        UUID commentAuthorId = UUID.randomUUID();

        CommentLikedNotificationContext context = parser.parse(event(
                actorId,
                commentAuthorId,
                "COMMENT",
                "comment-agg",
                "{}"
        ));

        assertEquals(commentAuthorId, context.commentAuthorId());
        assertEquals("comment-agg", context.commentId());
    }

    @Test
    void parse_acceptsCommentOwnerIdAlias() {
        UUID actorId = UUID.randomUUID();
        UUID commentAuthorId = UUID.randomUUID();

        CommentLikedNotificationContext context = parser.parse(event(
                actorId,
                null,
                null,
                null,
                """
                        {"actor_id":"%s","comment_owner_id":"%s","comment_id":"comment-1"}
                        """.formatted(actorId, commentAuthorId)
        ));

        assertEquals(commentAuthorId, context.commentAuthorId());
    }

    @Test
    void parse_throwsWhenCommentAuthorMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                UUID.randomUUID(),
                null,
                "COMMENT",
                "comment-1",
                """
                        {"actor_id":"%s","comment_id":"comment-1"}
                        """.formatted(UUID.randomUUID())
        )));
    }

    private NotificationEvent event(
            UUID actorId,
            UUID commentAuthorId,
            String aggregateType,
            String aggregateId,
            String payload
    ) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "COMMENT_LIKED",
                NotificationSourceService.SOCIAL,
                aggregateType,
                aggregateId,
                actorId,
                commentAuthorId,
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
