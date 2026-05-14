package com.twohands.auth_service.domain.session;

import com.twohands.auth_service.domain.shared.DomainError;

public final class SessionDomainError extends DomainError {
    public SessionDomainError(String code, String message) {
        super(code, message);
    }
}
