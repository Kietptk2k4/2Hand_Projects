package com.twohands.auth_service.application.rbac.assignrolestousers;

import java.util.UUID;

public record AssignRolesToUsersResult(
        UUID userId,
        UUID roleId
) {
}
