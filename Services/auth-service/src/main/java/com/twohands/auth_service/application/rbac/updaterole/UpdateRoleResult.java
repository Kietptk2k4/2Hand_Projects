package com.twohands.auth_service.application.rbac.updaterole;

import java.time.Instant;
import java.util.UUID;

public record UpdateRoleResult(
        UUID id,
        String code,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
