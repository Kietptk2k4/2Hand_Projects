package com.twohands.commerce_service.application.address.deleteuseraddress;

import java.util.UUID;

public record DeleteUserAddressCommand(
        UUID userId,
        UUID addressId
) {
}
