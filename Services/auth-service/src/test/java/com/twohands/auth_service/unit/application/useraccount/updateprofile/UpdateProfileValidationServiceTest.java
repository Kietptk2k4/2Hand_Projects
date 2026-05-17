package com.twohands.auth_service.unit.application.useraccount.updateprofile;

import com.twohands.auth_service.application.useraccount.updateprofile.UpdateProfileCommand;
import com.twohands.auth_service.application.useraccount.updateprofile.UpdateProfileValidationService;
import com.twohands.auth_service.exception.AppException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateProfileValidationServiceTest {

    private final UpdateProfileValidationService service = new UpdateProfileValidationService();

    @Test
    void shouldRejectInvalidWebsiteUrl() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validate(new UpdateProfileCommand(
                        UUID.randomUUID(),
                        "Kiet Tran",
                        "bio",
                        "ftp://invalid-url",
                        Map.of("github", "https://github.com/user")
                ))
        );

        assertEquals("URL format is invalid", ex.getMessage());
    }

    @Test
    void shouldRejectInvalidSocialLinkUrl() {
        AppException ex = assertThrows(
                AppException.class,
                () -> service.validate(new UpdateProfileCommand(
                        UUID.randomUUID(),
                        "Kiet Tran",
                        "bio",
                        "https://example.com",
                        Map.of("github", "bad-url")
                ))
        );

        assertEquals("URL format is invalid", ex.getMessage());
    }
}
