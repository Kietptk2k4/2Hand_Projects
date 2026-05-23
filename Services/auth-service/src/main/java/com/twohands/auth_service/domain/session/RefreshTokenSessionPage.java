package com.twohands.auth_service.domain.session;

import java.util.List;

public record RefreshTokenSessionPage(
        List<RefreshTokenSession> sessions,
        long totalItems
) {
}
