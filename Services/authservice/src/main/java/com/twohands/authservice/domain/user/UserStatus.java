package com.twohands.authservice.domain.user;

public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    TEMP_BANNED,
    PERMANENT_BANNED,
    UNDER_REVIEW,
    RESTRICTED,
    LOCKED,
    DELETED,
    HARD_DELETED
}
