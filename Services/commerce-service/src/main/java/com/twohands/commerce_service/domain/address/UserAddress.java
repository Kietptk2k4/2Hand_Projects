package com.twohands.commerce_service.domain.address;

import java.util.UUID;

public record UserAddress(
        UUID id,
        UUID userId,
        String provinceCode,
        String districtCode,
        String wardCode
) {
}
