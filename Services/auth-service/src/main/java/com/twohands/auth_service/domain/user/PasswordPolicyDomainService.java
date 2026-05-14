package com.twohands.auth_service.domain.user;

public final class PasswordPolicyDomainService {

    private static final int MIN_LENGTH = 8;

    public void ensureRawPasswordStrong(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new UserDomainError("USER_PASSWORD_TOO_WEAK", "Password must be at least 8 characters");
        }
    }

    public void ensureDifferentRawPassword(String oldRawPassword, String newRawPassword) {
        if (oldRawPassword != null && oldRawPassword.equals(newRawPassword)) {
            throw new UserDomainError("USER_PASSWORD_DUPLICATED", "New password must be different from old password");
        }
    }
}
