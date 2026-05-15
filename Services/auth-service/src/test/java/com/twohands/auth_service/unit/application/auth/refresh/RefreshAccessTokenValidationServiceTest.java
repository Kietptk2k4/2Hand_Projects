package com.twohands.auth_service.unit.application.auth.refresh;

import com.twohands.auth_service.application.auth.refresh.RefreshAccessTokenValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefreshAccessTokenValidationServiceTest {

    private final RefreshAccessTokenValidationService service = new RefreshAccessTokenValidationService();

    @Test
    void shouldRejectBlankRefreshToken() {
        AppException ex = assertThrows(AppException.class, () -> service.validateRefreshToken(" "));

        assertEquals("Refresh token is required", ex.getMessage());
    }
}
