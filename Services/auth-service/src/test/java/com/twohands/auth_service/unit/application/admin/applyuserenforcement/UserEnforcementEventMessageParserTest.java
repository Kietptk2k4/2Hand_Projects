package com.twohands.auth_service.unit.application.admin.applyuserenforcement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.admin.applyuserenforcement.ConsumeUserEnforcementEventCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.InvalidUserEnforcementEventException;
import com.twohands.auth_service.application.admin.applyuserenforcement.UserEnforcementEventMessageParser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserEnforcementEventMessageParserTest {

    private final UserEnforcementEventMessageParser parser = new UserEnforcementEventMessageParser(new ObjectMapper());

    @Test
    void shouldParseSuspendAdminOutboxEnvelope() {
        UUID eventId = UUID.fromString("a1111111-1111-1111-1111-111111111111");
        UUID userId = UUID.fromString("b2222222-2222-2222-2222-222222222222");
        UUID enforcementId = UUID.fromString("c3333333-3333-3333-3333-333333333333");

        String json = """
                {
                  "event_id": "a1111111-1111-1111-1111-111111111111",
                  "event_type": "USER_SUSPENDED",
                  "event_key": "user_enforcement:b2222222-2222-2222-2222-222222222222",
                  "aggregate_id": "c3333333-3333-3333-3333-333333333333",
                  "source": "admin",
                  "occurred_at": "2026-06-04T10:15:00Z",
                  "payload": {
                    "user_id": "b2222222-2222-2222-2222-222222222222",
                    "enforcement_id": "c3333333-3333-3333-3333-333333333333",
                    "action_type": "SUSPEND",
                    "reason_code": "POLICY_VIOLATION",
                    "description": "E2E 8B suspend test",
                    "expires_at": null,
                    "enforced_by": "d4444444-4444-4444-4444-444444444444",
                    "status": "ACTIVE"
                  }
                }
                """;

        ConsumeUserEnforcementEventCommand command = parser.parse(json, "admin.user.suspended", null);

        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.eventType()).isEqualTo("USER_SUSPENDED");
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.enforcementId()).isEqualTo(enforcementId);
        assertThat(command.actionType()).isEqualTo("SUSPEND");
        assertThat(command.reasonCode()).isEqualTo("POLICY_VIOLATION");
        assertThat(command.description()).isEqualTo("E2E 8B suspend test");
        assertThat(command.occurredAt()).isEqualTo(Instant.parse("2026-06-04T10:15:00Z"));
    }

    @Test
    void shouldParseRevokeAdminOutboxEnvelope() {
        UUID eventId = UUID.fromString("e5555555-5555-5555-5555-555555555555");
        UUID userId = UUID.fromString("b2222222-2222-2222-2222-222222222222");
        UUID enforcementId = UUID.fromString("c3333333-3333-3333-3333-333333333333");

        String json = """
                {
                  "event_id": "e5555555-5555-5555-5555-555555555555",
                  "event_type": "USER_ENFORCEMENT_REVOKED",
                  "source": "admin",
                  "occurred_at": "2026-06-04T11:00:00Z",
                  "payload": {
                    "user_id": "b2222222-2222-2222-2222-222222222222",
                    "enforcement_id": "c3333333-3333-3333-3333-333333333333",
                    "action_type": "SUSPEND",
                    "reason_code": "POLICY_VIOLATION",
                    "previous_status": "ACTIVE",
                    "new_status": "REVOKED",
                    "revoked_by": "d4444444-4444-4444-4444-444444444444",
                    "note": "E2E 8B revoke",
                    "revoke_reason": "Appeal accepted"
                  }
                }
                """;

        ConsumeUserEnforcementEventCommand command = parser.parse(json, "admin.user.enforcement_revoked", null);

        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.eventType()).isEqualTo("USER_ENFORCEMENT_REVOKED");
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.enforcementId()).isEqualTo(enforcementId);
        assertThat(command.actionType()).isEqualTo("SUSPEND");
        assertThat(command.description()).isEqualTo("Appeal accepted");
        assertThat(command.occurredAt()).isEqualTo(Instant.parse("2026-06-04T11:00:00Z"));
    }

    @Test
    void shouldResolveEventTypeFromTopicWhenMissingInEnvelope() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID enforcementId = UUID.randomUUID();

        String json = """
                {
                  "event_id": "%s",
                  "payload": {
                    "user_id": "%s",
                    "enforcement_id": "%s",
                    "action_type": "BAN",
                    "reason_code": "POLICY_VIOLATION",
                    "description": "Banned"
                  }
                }
                """.formatted(eventId, userId, enforcementId);

        ConsumeUserEnforcementEventCommand command = parser.parse(
                json,
                "admin.user.banned",
                "USER_BANNED"
        );

        assertThat(command.eventType()).isEqualTo("USER_BANNED");
    }

    @Test
    void shouldRejectMissingUserId() {
        String json = """
                {
                  "event_id": "%s",
                  "event_type": "USER_SUSPENDED",
                  "payload": {
                    "enforcement_id": "%s",
                    "action_type": "SUSPEND"
                  }
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> parser.parse(json, "admin.user.suspended", null))
                .isInstanceOf(InvalidUserEnforcementEventException.class)
                .hasMessageContaining("user_id");
    }
}
