package com.twohands.auth_service.application.useraccount.common;

import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserAccountAuthContextService {

    public UUID requireUserId(UUID userId) {
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return userId;
    }

    public void ensureUserActive(UserStatus status) {
        if (status == UserStatus.DELETED) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
    }
}
