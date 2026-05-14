package com.twohands.auth_service.domain.user;

import com.twohands.auth_service.domain.shared.DomainError;

public final class UserDomainError extends DomainError {
    public UserDomainError(String code, String message) {
        super(code, message);
    }
}
