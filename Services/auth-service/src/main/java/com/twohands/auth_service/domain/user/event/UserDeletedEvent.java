package com.twohands.auth_service.domain.user.event;

import com.twohands.auth_service.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserDeletedEvent(UUID userId, Instant occurredAt) implements DomainEvent {
}
