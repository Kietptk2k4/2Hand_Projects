package com.twohands.commerce_service.domain.address;

import java.util.UUID;

public record CreateUserAddressDraft(
        UUID userId,
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail,
        boolean isDefault
) {
}
