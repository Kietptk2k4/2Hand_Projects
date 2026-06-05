package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface CommerceShopGateway {

	boolean isEnabled();

	Optional<UUID> findShopOwnerId(UUID shopId);
}
