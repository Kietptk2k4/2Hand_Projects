package com.twohands.auth_service.application.auth.adminlogout;

import java.util.UUID;

public record AdminLogoutCommand(
        UUID authenticatedUserId,
        String refreshToken,
        String ipAddress
) {
}
