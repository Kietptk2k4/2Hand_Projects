package com.twohands.auth_service.unit.application.useraccount.updatesettings;

import com.twohands.auth_service.application.useraccount.updatesettings.UpdateUserSettingsValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateUserSettingsValidationServiceTest {

    private final UpdateUserSettingsValidationService service = new UpdateUserSettingsValidationService();

    @Test
    void shouldRejectInvalidAppearanceMode() {
        AppException ex = assertThrows(AppException.class, () -> service.validateAndParseAppearanceMode("BLUE"));
        assertEquals("Appearance mode must be LIGHT, DARK or SYSTEM", ex.getMessage());
    }
}
