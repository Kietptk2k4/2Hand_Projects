package com.twohands.social_service.unit.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxStatus;
import com.twohands.social_service.infrastructure.outbox.SocialOutboxEventKeyResolver;
import com.twohands.social_service.infrastructure.outbox.SocialOutboxMessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SocialOutboxMessageBuilderTest {

    private SocialOutboxMessageBuilder messageBuilder;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        messageBuilder = new SocialOutboxMessageBuilder(objectMapper, new SocialOutboxEventKeyResolver());
    }

    @Test
    void buildEnvelopeShouldSetActorAndRecipientFromPayload() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        String payloadJson = """
                {
                  "post_id": "post-1",
                  "actor_id": "%s",
                  "post_author_id": "%s"
                }
                """.formatted(actorId, recipientId);

        OutboxEvent event = new OutboxEvent(
                eventId,
                "POST_LIKED",
                "post-1",
                payloadJson,
                OutboxStatus.PENDING,
                0,
                Instant.parse("2026-06-04T10:00:00Z"),
                null,
                null
        );

        Map<String, Object> envelope = messageBuilder.buildEnvelope(event);

        assertThat(envelope.get("actor_id")).isEqualTo(actorId.toString());
        assertThat(envelope.get("recipient_user_ids")).isEqualTo(List.of(recipientId.toString()));
    }

    @Test
    void buildEnvelopeShouldSetRecipientListFromFollowerUserIds() {
        UUID eventId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID followerA = UUID.randomUUID();
        UUID followerB = UUID.randomUUID();
        String payloadJson = """
                {
                  "actor_id": "%s",
                  "user_id": "%s",
                  "avatar_url": "https://cdn.2hands.vn/new.png",
                  "follower_user_ids": ["%s", "%s"]
                }
                """.formatted(actorId, actorId, followerA, followerB);

        OutboxEvent event = new OutboxEvent(
                eventId,
                "USER_AVATAR_UPDATED",
                actorId.toString(),
                payloadJson,
                OutboxStatus.PENDING,
                0,
                Instant.parse("2026-06-04T10:00:00Z"),
                null,
                null
        );

        Map<String, Object> envelope = messageBuilder.buildEnvelope(event);

        assertThat(envelope.get("recipient_user_ids")).isEqualTo(List.of(followerA.toString(), followerB.toString()));
    }
}
