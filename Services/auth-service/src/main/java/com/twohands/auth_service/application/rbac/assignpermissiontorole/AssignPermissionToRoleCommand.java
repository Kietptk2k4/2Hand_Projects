package com.twohands.auth_service.application.rbac.assignpermissiontorole;

import java.util.UUID;

public record AssignPermissionToRoleCommand(
        UUID actorUserId,
        UUID roleId,
        String permissionCode
) {
}
