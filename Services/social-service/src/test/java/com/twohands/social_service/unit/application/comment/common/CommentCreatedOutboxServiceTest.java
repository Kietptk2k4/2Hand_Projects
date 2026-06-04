package com.twohands.social_service.unit.application.comment.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.comment.common.CommentCreatedOutboxService;
import com.twohands.social_service.domain.comment.Comment;
import com.twohands.social_service.domain.comment.CommentStatus;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentCreatedOutboxServiceTest {

    private CommentCreatedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new CommentCreatedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeActorAndPostAuthor() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        Comment comment = new Comment(
                "comment-1",
                "post-1",
                authorId.toString(),
                null,
                "hello",
                List.of(),
                CommentStatus.ACTIVE,
                0L,
                now,
                now,
                null
        );

        OutboxEvent event = outboxService.build(comment, postAuthorId.toString(), now);

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("comment_id").asText()).isEqualTo("comment-1");
        assertThat(payload.get("actor_id").asText()).isEqualTo(authorId.toString());
        assertThat(payload.get("author_id").asText()).isEqualTo(authorId.toString());
        assertThat(payload.get("post_author_id").asText()).isEqualTo(postAuthorId.toString());
        assertThat(payload.has("parent_comment_id")).isFalse();
    }
}
