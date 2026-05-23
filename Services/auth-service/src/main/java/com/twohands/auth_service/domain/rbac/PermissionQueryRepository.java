package com.twohands.auth_service.domain.rbac;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionQueryRepository {
    Set<String> findPermissionCodesByRoleIds(Set<UUID> roleIds);

    Set<String> findPermissionCodesByUserId(UUID userId);

    List<String> findRoleCodesByUserId(UUID userId);

    List<PermissionData> findPermissionsByRoleId(UUID roleId);

    record PermissionData(String code, String description) {
    }
}
