package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.UUID;

public record Cart(UUID id, UUID userId, Instant createdAt, Instant updatedAt) {
}
