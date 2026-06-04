package com.twohands.auth_service.unit.application.auth.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.common.UserCreatedOutboxService;
import com.twohands.auth_service.application.useraccount.common.UserProjectionSyncPayload;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreatedOutboxServiceTest {

    private UserCreatedOutboxService outboxService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        outboxService = new UserCreatedOutboxService(objectMapper);
    }

    @Test
    void buildWithDisplayNameShouldIncludeProfileFields() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-04T10:00:00Z");

        OutboxEvent event = outboxService.build(
                userId,
                "u@example.com",
                "PENDING_VERIFICATION",
                now,
                UserProjectionSyncPayload.profileOnly("u", null, false)
        );

        assertThat(event.eventType()).isEqualTo("USER_CREATED");

        JsonNode payload = objectMapper.readTree(event.payload());
        assertThat(payload.get("display_name").asText()).isEqualTo("u");
        assertThat(payload.get("is_private").asBoolean()).isFalse();
        assertThat(payload.get("status").asText()).isEqualTo("PENDING_VERIFICATION");
        assertThat(payload.has("avatar_url")).isFalse();
    }
}
