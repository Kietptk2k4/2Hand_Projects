package com.twohands.auth_service.unit.application.auth.forgotpassword;

import com.twohands.auth_service.application.auth.forgotpassword.ForgotPasswordValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForgotPasswordValidationServiceTest {

    private final ForgotPasswordValidationService service = new ForgotPasswordValidationService();

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
}
