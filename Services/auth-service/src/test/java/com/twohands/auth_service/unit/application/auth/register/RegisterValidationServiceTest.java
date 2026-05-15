package com.twohands.auth_service.unit.application.auth.register;

import com.twohands.auth_service.application.auth.register.RegisterValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterValidationServiceTest {

    private final RegisterValidationService service = new RegisterValidationService();

    @Test
    void shouldNormalizeEmailToLowercase() {
        String normalized = service.normalizeAndValidateEmail(" User@Example.com ");

        assertEquals("user@example.com", normalized);
    }

    @Test
    void shouldRejectWeakPassword() {
        AppException ex = assertThrows(AppException.class, () -> service.validatePassword("weakpass"));

        assertEquals("Mat khau phai tu 8-32 ky tu, bao gom chu hoa, chu thuong va so", ex.getMessage());
    }

    @Test
    void shouldRejectConfirmPasswordMismatch() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validateConfirmPassword("Password123", "Password124")
        );

        assertEquals("Confirm password does not match password", ex.getMessage());
    }
}
