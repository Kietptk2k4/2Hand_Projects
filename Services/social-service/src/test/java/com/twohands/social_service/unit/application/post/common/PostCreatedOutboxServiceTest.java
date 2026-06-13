package com.twohands.social_service.unit.application.post.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.post.common.PostCreatedOutboxService;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostCreatedOutboxServiceTest {

    private PostCreatedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new PostCreatedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludePostAuthorAndFollowers() throws Exception {
        UUID authorId = UUID.randomUUID();
        UUID followerA = UUID.randomUUID();
        UUID followerB = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.build(
                "post-1",
                authorId,
                "Alice",
                "PUBLIC",
                "Hello followers",
                List.of(followerA, followerB),
                now
        );

        assertThat(event.eventType()).isEqualTo("POST_CREATED");
        assertThat(event.aggregateId()).isEqualTo("post-1");

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("post_id").asText()).isEqualTo("post-1");
        assertThat(payload.get("actor_id").asText()).isEqualTo(authorId.toString());
        assertThat(payload.get("post_author_id").asText()).isEqualTo(authorId.toString());
        assertThat(payload.get("actor_display_name").asText()).isEqualTo("Alice");
        assertThat(payload.get("visibility").asText()).isEqualTo("PUBLIC");
        assertThat(payload.get("caption_preview").asText()).isEqualTo("Hello followers");
        assertThat(payload.get("follower_user_ids")).hasSize(2);
        assertThat(payload.get("follower_user_ids").get(0).asText()).isEqualTo(followerA.toString());
        assertThat(payload.get("follower_user_ids").get(1).asText()).isEqualTo(followerB.toString());
    }
}
