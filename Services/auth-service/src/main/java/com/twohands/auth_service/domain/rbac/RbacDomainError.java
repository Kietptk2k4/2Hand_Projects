package com.twohands.auth_service.domain.rbac;

import com.twohands.auth_service.domain.shared.DomainError;

public final class RbacDomainError extends DomainError {
    public RbacDomainError(String code, String message) {
        super(code, message);
    }
}
