package com.twohands.auth_service.application.auth.forgotpassword;

public record ForgotPasswordCommand(
        String email,
        String ipAddress
) {
}
