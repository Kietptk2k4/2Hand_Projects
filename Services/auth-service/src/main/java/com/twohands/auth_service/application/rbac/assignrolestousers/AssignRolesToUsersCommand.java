package com.twohands.auth_service.application.rbac.assignrolestousers;

import java.util.UUID;

public record AssignRolesToUsersCommand(
        UUID actorUserId,
        UUID targetUserId,
        UUID roleId
) {
}
