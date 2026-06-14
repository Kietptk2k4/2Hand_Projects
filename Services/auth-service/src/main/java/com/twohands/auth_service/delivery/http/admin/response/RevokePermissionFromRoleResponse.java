package com.twohands.auth_service.delivery.http.admin.response;

public record RevokePermissionFromRoleResponse(
        String roleId,
        String permissionCode
) {
}
