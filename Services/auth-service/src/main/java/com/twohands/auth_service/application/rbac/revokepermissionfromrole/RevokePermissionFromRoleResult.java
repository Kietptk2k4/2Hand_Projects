package com.twohands.auth_service.application.rbac.revokepermissionfromrole;

import java.util.UUID;

public record RevokePermissionFromRoleResult(
        UUID roleId,
        String permissionCode
) {
}
