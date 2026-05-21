package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.UUID;

public record DeleteUserAddressResult(
        UUID addressId,
        UUID userId,
        boolean wasDefault,
        UUID newDefaultAddressId,
        Instant deletedAt
) {
}
