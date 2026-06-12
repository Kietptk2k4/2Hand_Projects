package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface CommerceShopGateway {

	boolean isEnabled();

	Optional<UUID> findShopOwnerId(UUID shopId);

	void suspendShop(UUID shopId, UUID adminId, String reason);

	void closeShop(UUID shopId, UUID adminId, String reason);

	void restoreShop(UUID shopId, UUID adminId, String reason);
}
