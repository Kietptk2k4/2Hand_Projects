package com.twohands.auth_service.domain.session;

import java.util.UUID;

public final class SessionOwnershipDomainService {

    public void ensureOwner(UUID actorUserId, RefreshTokenSession targetSession) {
        if (!targetSession.belongsTo(actorUserId)) {
            throw new SessionDomainError("SESSION_OWNER_FORBIDDEN", "Cannot manage another user's session");
        }
    }
}
