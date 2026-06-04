package com.twohands.auth_service.unit.application.auth.verifyemail;

import com.twohands.auth_service.application.auth.verifyemail.VerifyEmailValidationService;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VerifyEmailValidationServiceTest {

    private final VerifyEmailValidationService service = new VerifyEmailValidationService();

    @Test
    void validateAndNormalizeToken_acceptsSixDigits() {
        assertEquals("123456", service.validateAndNormalizeToken("123456"));
        assertEquals("123456", service.validateAndNormalizeToken(" 123456 "));
    }

    @Test
    void validateAndNormalizeToken_rejectsNonSixDigit() {
        AppException ex = assertThrows(AppException.class, () -> service.validateAndNormalizeToken("12345"));
        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        assertEquals("INVALID_FORMAT", ex.getReason());
    }

    @Test
    void validateAndNormalizeToken_rejectsHexToken() {
        assertThrows(AppException.class, () -> service.validateAndNormalizeToken("a1b2c3d4e5f6"));
    }

    @Test
    void validateAndNormalizeToken_rejectsBlank() {
        AppException ex = assertThrows(AppException.class, () -> service.validateAndNormalizeToken("  "));
        assertEquals("REQUIRED", ex.getReason());
    }
}
