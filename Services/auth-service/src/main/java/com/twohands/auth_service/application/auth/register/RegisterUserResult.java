package com.twohands.auth_service.application.auth.register;

public record RegisterUserResult(
        String userId,
        String email,
        String status
) {
}
