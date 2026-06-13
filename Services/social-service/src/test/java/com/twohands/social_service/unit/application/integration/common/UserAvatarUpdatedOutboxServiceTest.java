package com.twohands.social_service.unit.application.integration.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.integration.common.UserAvatarUpdatedOutboxService;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserAvatarUpdatedOutboxServiceTest {

    private UserAvatarUpdatedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new UserAvatarUpdatedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeActorAvatarAndFollowers() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID followerA = UUID.randomUUID();
        UUID followerB = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.build(
                userId,
                "User A",
                "https://cdn.2hands.vn/new.png",
                List.of(followerA, followerB),
                now
        );

        assertThat(event.eventType()).isEqualTo("USER_AVATAR_UPDATED");
        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("actor_id").asText()).isEqualTo(userId.toString());
        assertThat(payload.get("avatar_url").asText()).isEqualTo("https://cdn.2hands.vn/new.png");
        assertThat(payload.get("follower_user_ids")).hasSize(2);
        assertThat(payload.get("follower_user_ids").get(0).asText()).isEqualTo(followerA.toString());
        assertThat(payload.get("follower_user_ids").get(1).asText()).isEqualTo(followerB.toString());
    }
}
