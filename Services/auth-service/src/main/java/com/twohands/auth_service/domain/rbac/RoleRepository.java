package com.twohands.auth_service.domain.rbac;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Optional<Role> findById(UUID roleId);

    Optional<Role> findByCode(String code);

    Role save(Role role);

    void deleteById(UUID roleId);
}
