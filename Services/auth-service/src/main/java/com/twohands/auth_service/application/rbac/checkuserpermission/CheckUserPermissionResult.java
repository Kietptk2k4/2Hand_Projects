package com.twohands.auth_service.application.rbac.checkuserpermission;

import java.util.List;
import java.util.UUID;

public record CheckUserPermissionResult(
        UUID userId,
        List<PermissionData> permissions
) {
    public record PermissionData(
            String code
    ) {
    }
}
