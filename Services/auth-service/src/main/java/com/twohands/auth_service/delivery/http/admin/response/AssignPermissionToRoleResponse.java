package com.twohands.auth_service.delivery.http.admin.response;

public record AssignPermissionToRoleResponse(
        String roleId,
        String permissionCode
) {
}
