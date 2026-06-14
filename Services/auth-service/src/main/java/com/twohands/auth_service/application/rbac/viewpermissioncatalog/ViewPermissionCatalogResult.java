package com.twohands.auth_service.application.rbac.viewpermissioncatalog;

import java.util.List;

public record ViewPermissionCatalogResult(
        List<PermissionData> permissions
) {
    public record PermissionData(String code, String description) {
    }
}
