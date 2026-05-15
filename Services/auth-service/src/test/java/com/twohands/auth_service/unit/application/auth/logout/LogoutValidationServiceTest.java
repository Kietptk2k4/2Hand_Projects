package com.twohands.auth_service.unit.application.auth.logout;

import com.twohands.auth_service.application.auth.logout.LogoutValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogoutValidationServiceTest {

    private final LogoutValidationService service = new LogoutValidationService();

    @Test
    void shouldRejectBlankRefreshToken() {
        AppException ex = assertThrows(AppException.class, () -> service.validateRefreshToken(" "));

        assertEquals("Refresh token is required", ex.getMessage());
    }
}
