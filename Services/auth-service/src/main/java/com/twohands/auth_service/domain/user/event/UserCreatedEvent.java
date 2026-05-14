package com.twohands.auth_service.domain.user.event;

import com.twohands.auth_service.domain.shared.DomainEvent;
import com.twohands.auth_service.domain.user.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email,
        UserStatus status,
        Instant occurredAt
) implements DomainEvent {
}
