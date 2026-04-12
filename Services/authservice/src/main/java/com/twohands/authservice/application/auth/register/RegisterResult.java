package com.twohands.authservice.application.auth.register;

import java.util.UUID;

public record RegisterResult(UUID userId, String status) {
}
