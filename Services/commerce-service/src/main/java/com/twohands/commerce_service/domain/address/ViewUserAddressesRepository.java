package com.twohands.commerce_service.domain.address;

import java.util.UUID;

public interface ViewUserAddressesRepository {

    ViewUserAddressesResult findByUserId(UUID userId);
}
