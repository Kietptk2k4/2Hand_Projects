package com.twohands.social_service.unit.application.post.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.post.common.PostLikedOutboxService;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikedOutboxServiceTest {

    private PostLikedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new PostLikedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeActorAndPostAuthor() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.build("post-1", actorId, postAuthorId.toString(), now);

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("post_id").asText()).isEqualTo("post-1");
        assertThat(payload.get("actor_id").asText()).isEqualTo(actorId.toString());
        assertThat(payload.get("post_author_id").asText()).isEqualTo(postAuthorId.toString());
        assertThat(payload.get("user_id").asText()).isEqualTo(actorId.toString());
        assertThat(payload.has("avatar_url")).isFalse();
    }
}
