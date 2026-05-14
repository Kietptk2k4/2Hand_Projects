package com.twohands.auth_service.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailAddressTest {

    @Test
    void shouldNormalizeEmailToLowercase() {
        EmailAddress email = EmailAddress.of("  Test.User@Example.COM  ");

        assertEquals("test.user@example.com", email.normalizedValue());
        assertEquals("Test.User@Example.COM", email.value());
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        UserDomainError error = assertThrows(UserDomainError.class, () -> EmailAddress.of("invalid-email"));

        assertEquals("USER_EMAIL_INVALID", error.code());
    }
}
