package com.twohands.commerce_service.application.address.viewuseraddresses;

import java.util.UUID;

public record ViewUserAddressesCommand(
        UUID userId
) {
}
