package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DeleteUserAddressRepository {

    Optional<OwnedUserAddress> findOwnedAddress(UUID addressId, UUID userId);

    DeleteUserAddressResult delete(OwnedUserAddress address, Instant occurredAt);
}
