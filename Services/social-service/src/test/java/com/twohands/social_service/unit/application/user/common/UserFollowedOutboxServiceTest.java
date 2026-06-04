package com.twohands.social_service.unit.application.user.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.application.user.common.UserFollowedOutboxService;
import com.twohands.social_service.domain.follow.FollowStatus;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserFollowedOutboxServiceTest {

    private UserFollowedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new UserFollowedOutboxService(objectMapper);
    }

    @Test
    void buildShouldIncludeActorAndFollowedUser() throws Exception {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.build(followerId, followeeId, FollowStatus.ACCEPTED, now);

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("actor_id").asText()).isEqualTo(followerId.toString());
        assertThat(payload.get("followed_user_id").asText()).isEqualTo(followeeId.toString());
        assertThat(payload.get("follower_id").asText()).isEqualTo(followerId.toString());
        assertThat(payload.get("followee_id").asText()).isEqualTo(followeeId.toString());
        assertThat(payload.get("status").asText()).isEqualTo("ACCEPTED");
    }
}
