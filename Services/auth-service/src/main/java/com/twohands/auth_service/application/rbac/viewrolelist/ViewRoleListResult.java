package com.twohands.auth_service.application.rbac.viewrolelist;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewRoleListResult(
        List<RoleData> roles
) {
    public record RoleData(
            UUID id,
            String code,
            String name,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
