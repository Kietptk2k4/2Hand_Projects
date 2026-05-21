package com.twohands.commerce_service.domain.address;

import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository {

    Optional<UserAddress> findByIdAndUserId(UUID addressId, UUID userId);
}
