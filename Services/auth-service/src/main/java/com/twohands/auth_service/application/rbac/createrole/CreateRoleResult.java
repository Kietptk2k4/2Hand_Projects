package com.twohands.auth_service.application.rbac.createrole;

import java.time.Instant;
import java.util.UUID;

public record CreateRoleResult(
        UUID id,
        String code,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
