package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface CommerceProductGateway {

	boolean isEnabled();

	void ensureProductExists(UUID productId);

	Optional<UUID> findSellerUserId(UUID productId);

	void removeProduct(UUID productId, UUID adminId, String reason);

	void restoreProduct(UUID productId, UUID adminId, String reason);
}
