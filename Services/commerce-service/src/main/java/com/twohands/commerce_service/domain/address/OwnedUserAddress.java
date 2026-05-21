package com.twohands.commerce_service.domain.address;

import java.util.UUID;

public record OwnedUserAddress(
        UUID addressId,
        UUID userId,
        boolean isDefault
) {
}
