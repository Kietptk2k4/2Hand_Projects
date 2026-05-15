package com.twohands.auth_service.application.auth.changepassword;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID userId,
        String currentPassword,
        String newPassword,
        String confirmNewPassword
) {
}
