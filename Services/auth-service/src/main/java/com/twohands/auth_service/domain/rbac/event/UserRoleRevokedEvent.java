package com.twohands.auth_service.domain.rbac.event;

import com.twohands.auth_service.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserRoleRevokedEvent(
        UUID userId,
        UUID roleId,
        Instant occurredAt
) implements DomainEvent {
}
