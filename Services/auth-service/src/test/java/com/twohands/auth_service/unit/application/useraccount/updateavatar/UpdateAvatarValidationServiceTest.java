package com.twohands.auth_service.unit.application.useraccount.updateavatar;

import com.twohands.auth_service.application.useraccount.updateavatar.UpdateAvatarCommand;
import com.twohands.auth_service.application.useraccount.updateavatar.UpdateAvatarValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateAvatarValidationServiceTest {

    private final UpdateAvatarValidationService service = new UpdateAvatarValidationService();

    @Test
    void shouldRejectInvalidAvatarUrl() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validate(new UpdateAvatarCommand(UUID.randomUUID(), "javascript:alert(1)"))
        );

        assertEquals("Avatar URL format is invalid", ex.getMessage());
    }
}
