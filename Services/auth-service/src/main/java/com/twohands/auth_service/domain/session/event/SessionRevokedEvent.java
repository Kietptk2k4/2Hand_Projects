package com.twohands.auth_service.domain.session.event;

import com.twohands.auth_service.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SessionRevokedEvent(
        UUID sessionId,
        UUID userId,
        Instant occurredAt
) implements DomainEvent {
}
