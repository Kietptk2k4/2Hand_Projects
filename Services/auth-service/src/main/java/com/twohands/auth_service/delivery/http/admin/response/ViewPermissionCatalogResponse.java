package com.twohands.auth_service.delivery.http.admin.response;

import java.util.List;

public record ViewPermissionCatalogResponse(
        List<PermissionData> permissions
) {
    public record PermissionData(
            String code,
            String description
    ) {
    }
}
