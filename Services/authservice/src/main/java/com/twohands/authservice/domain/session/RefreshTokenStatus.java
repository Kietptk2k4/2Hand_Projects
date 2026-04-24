package com.twohands.authservice.domain.session;

public enum RefreshTokenStatus {
    ACTIVE,
    EXPIRED,
    REVOKED,
    COMPROMISED,
    LOGGED_OUT,
    ROTATED
}
