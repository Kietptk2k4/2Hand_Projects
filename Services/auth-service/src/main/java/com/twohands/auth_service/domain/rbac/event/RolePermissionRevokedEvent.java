package com.twohands.auth_service.domain.rbac.event;

import com.twohands.auth_service.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RolePermissionRevokedEvent(
        UUID roleId,
        UUID permissionId,
        Instant occurredAt
) implements DomainEvent {
}
