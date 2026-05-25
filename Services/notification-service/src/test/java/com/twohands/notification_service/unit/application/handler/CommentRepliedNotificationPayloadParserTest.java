package com.twohands.notification_service.unit.application.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.application.handler.CommentRepliedNotificationPayloadParser;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import com.twohands.notification_service.domain.social.CommentRepliedNotificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommentRepliedNotificationPayloadParserTest {

    private CommentRepliedNotificationPayloadParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommentRepliedNotificationPayloadParser(new ObjectMapper());
    }

    @Test
    void parse_resolvesFieldsFromPayload() {
        UUID actorId = UUID.randomUUID();
        UUID parentAuthorId = UUID.randomUUID();

        CommentRepliedNotificationContext context = parser.parse(event(
                actorId,
                null,
                null,
                null,
                """
                        {
                          "actor_id": "%s",
                          "parent_comment_author_id": "%s",
                          "parent_comment_id": "parent-comment-1",
                          "comment_id": "reply-comment-9"
                        }
                        """.formatted(actorId, parentAuthorId)
        ));

        assertEquals(actorId, context.actorId());
        assertEquals(parentAuthorId, context.parentCommentAuthorId());
        assertEquals("parent-comment-1", context.parentCommentId());
        assertEquals("reply-comment-9", context.commentId());
    }

    @Test
    void parse_fallsBackToEventColumns() {
        UUID actorId = UUID.randomUUID();
        UUID parentAuthorId = UUID.randomUUID();

        CommentRepliedNotificationContext context = parser.parse(event(
                actorId,
                parentAuthorId,
                "COMMENT",
                "reply-comment-agg",
                """
                        {"parent_comment_id":"parent-comment-2"}
                        """
        ));

        assertEquals(parentAuthorId, context.parentCommentAuthorId());
        assertEquals("parent-comment-2", context.parentCommentId());
        assertEquals("reply-comment-agg", context.commentId());
    }

    @Test
    void parse_throwsWhenParentCommentAuthorMissing() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                UUID.randomUUID(),
                null,
                "COMMENT",
                "reply-1",
                """
                        {"actor_id":"%s","parent_comment_id":"parent-1","comment_id":"reply-1"}
                        """.formatted(UUID.randomUUID())
        )));
    }

    @Test
    void parse_throwsWhenParentCommentIdMissing() {
        UUID actorId = UUID.randomUUID();
        UUID parentAuthorId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> parser.parse(event(
                actorId,
                parentAuthorId,
                "COMMENT",
                "reply-1",
                """
                        {"actor_id":"%s","parent_comment_author_id":"%s","comment_id":"reply-1"}
                        """.formatted(actorId, parentAuthorId)
        )));
    }

    private NotificationEvent event(
            UUID actorId,
            UUID parentAuthorId,
            String aggregateType,
            String aggregateId,
            String payload
    ) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "COMMENT_REPLIED",
                NotificationSourceService.SOCIAL,
                aggregateType,
                aggregateId,
                actorId,
                parentAuthorId,
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
