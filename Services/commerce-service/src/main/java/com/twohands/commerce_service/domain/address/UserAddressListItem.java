package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.UUID;

public record UserAddressListItem(
        UUID id,
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
