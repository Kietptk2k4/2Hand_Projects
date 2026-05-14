package com.twohands.auth_service.domain.rbac;

import java.util.Optional;
import java.util.UUID;

public interface UserRoleAssignmentRepository {
    Optional<UserRoleAssignment> findByUserId(UUID userId);

    UserRoleAssignment save(UserRoleAssignment assignment);

    long countUsersByRoleId(UUID roleId);
}
