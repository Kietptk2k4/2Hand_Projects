package com.twohands.commerce_service.application.address.updateuseraddress;

import java.util.UUID;

public record UpdateUserAddressCommand(
        UUID userId,
        UUID addressId,
        String receiverName,
        String phone,
        String provinceCode,
        String districtCode,
        String wardCode,
        String addressDetail,
        Boolean isDefault
) {
}
