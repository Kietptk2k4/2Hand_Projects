package com.twohands.auth_service.domain.user;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record EmailAddress(String value, String normalizedValue) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new UserDomainError("USER_EMAIL_REQUIRED", "Email is required");
        }
        value = value.trim();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new UserDomainError("USER_EMAIL_INVALID", "Email format is invalid");
        }
        normalizedValue = value.toLowerCase(Locale.ROOT);
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value, normalize(value));
    }

    public boolean equalsNormalized(String candidate) {
        return normalizedValue.equals(normalize(candidate));
    }

    private static String normalize(String value) {
        Objects.requireNonNull(value, "Email cannot be null");
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
