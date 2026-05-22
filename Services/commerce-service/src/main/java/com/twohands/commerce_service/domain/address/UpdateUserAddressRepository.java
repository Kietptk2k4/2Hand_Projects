package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UpdateUserAddressRepository {

    Optional<UpdateUserAddressSnapshot> findByIdAndUserId(UUID addressId, UUID userId);

    UpdateUserAddressResult update(UpdateUserAddressDraft draft, Instant updatedAt);
}
