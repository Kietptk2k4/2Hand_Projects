package com.twohands.auth_service.application.rbac.revokerolefromuser;

import java.util.UUID;

public record RevokeRoleFromUserResult(
        UUID userId,
        UUID roleId
) {
}
