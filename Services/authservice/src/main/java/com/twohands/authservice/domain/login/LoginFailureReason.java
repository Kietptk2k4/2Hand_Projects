package com.twohands.authservice.domain.login;

public enum LoginFailureReason {
    INVALID_PASSWORD,
    ACCOUNT_LOCKED,
    ACCOUNT_BANNED,
    EMAIL_NOT_VERIFIED,
    OAUTH_REJECTED,
    TOKEN_EXPIRED,
    RATE_LIMITED,
    SUSPICIOUS_ACTIVITY
}
