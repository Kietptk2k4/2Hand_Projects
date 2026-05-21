package com.twohands.commerce_service.application.address.createuseraddress;

import java.util.UUID;

public record CreateUserAddressCommand(
        UUID userId,
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail,
        Boolean isDefault
) {
}
