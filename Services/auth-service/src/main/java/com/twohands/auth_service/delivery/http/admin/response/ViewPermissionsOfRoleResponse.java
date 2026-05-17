package com.twohands.auth_service.delivery.http.admin.response;

import java.util.List;

public record ViewPermissionsOfRoleResponse(
        RoleData role,
        List<PermissionData> permissions
) {
    public record RoleData(
            String id,
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
