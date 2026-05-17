package com.twohands.auth_service.application.rbac.revokerolefromuser;

import java.util.UUID;

public record RevokeRoleFromUserCommand(
        UUID actorUserId,
        UUID targetUserId,
        UUID roleId
) {
}
