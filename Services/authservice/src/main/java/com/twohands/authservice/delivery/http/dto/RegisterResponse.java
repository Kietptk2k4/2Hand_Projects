package com.twohands.authservice.delivery.http.dto;

import java.util.UUID;

public record RegisterResponse(UUID userId, String status, String message) {
}
