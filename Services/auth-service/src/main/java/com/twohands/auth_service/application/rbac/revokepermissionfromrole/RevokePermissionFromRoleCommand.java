package com.twohands.auth_service.application.rbac.revokepermissionfromrole;

import java.util.UUID;

public record RevokePermissionFromRoleCommand(
        UUID actorUserId,
        UUID roleId,
        String permissionCode
) {
}
