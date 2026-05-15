package com.twohands.auth_service.unit.application.auth.login;

import com.twohands.auth_service.application.auth.login.LoginValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginValidationServiceTest {

    private final LoginValidationService service = new LoginValidationService();

    @Test
    void shouldNormalizeEmailToLowercase() {
        String normalized = service.normalizeAndValidateEmail(" User@Example.com ");

        assertEquals("user@example.com", normalized);
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        AppException ex = assertThrows(AppException.class, () -> service.normalizeAndValidateEmail("invalid-email"));

        assertEquals("Email format is invalid", ex.getMessage());
    }

    @Test
    void shouldRejectBlankPassword() {
        AppException ex = assertThrows(AppException.class, () -> service.validatePassword(" "));

        assertEquals("Password is required", ex.getMessage());
    }
}
