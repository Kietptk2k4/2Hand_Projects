package com.twohands.auth_service.unit.application.useraccount.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.useraccount.common.UserAccountOutboxService;
import com.twohands.auth_service.application.useraccount.common.UserProjectionSyncPayload;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountOutboxServiceTest {

    private UserAccountOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new UserAccountOutboxService(objectMapper);
    }

    @Test
    void userUpdatedWithDisplayNameShouldOmitNullAvatarUrl() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.userUpdated(
                userId,
                "user@example.com",
                now,
                UserProjectionSyncPayload.profileOnly("Alice", null, null)
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("display_name").asText()).isEqualTo("Alice");
        assertThat(payload.has("avatar_url")).isFalse();
        assertThat(payload.has("is_private")).isFalse();
        assertThat(payload.get("user_id").asText()).isEqualTo(userId.toString());
    }

    @Test
    void userUpdatedWithIsPrivateOnlyShouldIncludeIsPrivate() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.userUpdated(
                userId,
                "user@example.com",
                now,
                UserProjectionSyncPayload.profileOnly(null, null, true)
        );

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("is_private").asBoolean()).isTrue();
        assertThat(payload.has("display_name")).isFalse();
        assertThat(payload.has("avatar_url")).isFalse();
    }

    @Test
    void userActivatedAfterEmailVerificationShouldIncludeActiveStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.userActivatedAfterEmailVerification(userId, "user@example.com", now);

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("status").asText()).isEqualTo("ACTIVE");
        assertThat(payload.get("email_verified").asBoolean()).isTrue();
        assertThat(payload.has("display_name")).isFalse();
    }
}
