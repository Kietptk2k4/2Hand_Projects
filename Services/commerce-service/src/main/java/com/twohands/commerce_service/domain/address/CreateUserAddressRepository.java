package com.twohands.commerce_service.domain.address;

import java.time.Instant;
import java.util.UUID;

public interface CreateUserAddressRepository {

    boolean hasAnyAddress(UUID userId);

    CreateUserAddressResult create(CreateUserAddressDraft draft, Instant occurredAt);
}
