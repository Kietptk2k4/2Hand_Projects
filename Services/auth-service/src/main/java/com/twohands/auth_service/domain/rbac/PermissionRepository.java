package com.twohands.auth_service.domain.rbac;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    Optional<Permission> findById(UUID permissionId);

    Optional<Permission> findByCode(String code);

    Permission save(Permission permission);

    void deleteById(UUID permissionId);
}
