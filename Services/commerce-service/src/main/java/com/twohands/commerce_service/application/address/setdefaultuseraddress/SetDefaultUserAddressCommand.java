package com.twohands.commerce_service.application.address.setdefaultuseraddress;

import java.util.UUID;

public record SetDefaultUserAddressCommand(
        UUID userId,
        UUID addressId
) {
}
