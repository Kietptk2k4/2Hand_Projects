package com.twohands.auth_service.application.rbac.deleterole;

import java.util.UUID;

public record DeleteRoleCommand(
        UUID actorUserId,
        UUID roleId
) {
}
