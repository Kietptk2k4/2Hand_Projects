package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SetDefaultUserAddressRepository {

    Optional<OwnedUserAddress> findOwnedAddress(UUID addressId, UUID userId);

    SetDefaultUserAddressResult setDefault(OwnedUserAddress address, Instant occurredAt);
}
