package com.twohands.auth_service.application.rbac.assignpermissiontorole;

import java.util.UUID;

public record AssignPermissionToRoleResult(
        UUID roleId,
        String permissionCode
) {
}
