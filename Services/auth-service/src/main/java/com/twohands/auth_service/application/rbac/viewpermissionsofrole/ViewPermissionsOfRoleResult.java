package com.twohands.auth_service.application.rbac.viewpermissionsofrole;

import java.util.List;
import java.util.UUID;

public record ViewPermissionsOfRoleResult(
        RoleData role,
        List<PermissionData> permissions
) {
    public record RoleData(
            UUID id,
            String code,
            String name
    ) {
    }

    public record PermissionData(
            String code,
            String description
    ) {
    }
}
