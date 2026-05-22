package com.twohands.commerce_service.domain.address;

import java.util.List;

public record ViewUserAddressesResult(
        List<UserAddressListItem> addresses
) {
}
