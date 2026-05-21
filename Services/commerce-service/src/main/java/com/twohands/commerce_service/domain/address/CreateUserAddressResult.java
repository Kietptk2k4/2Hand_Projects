package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.UUID;

public record CreateUserAddressResult(
        UUID addressId,
        UUID userId,
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail,
        boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
