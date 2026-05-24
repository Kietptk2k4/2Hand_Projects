package com.twohands.notification_service.security;

import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public final class AuthenticationSupport {

    private AuthenticationSupport() {
    }

    public static UUID requireUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return user.userId();
    }
}
