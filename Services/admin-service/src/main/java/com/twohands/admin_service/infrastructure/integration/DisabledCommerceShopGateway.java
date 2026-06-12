package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceShopGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceShopGateway implements CommerceShopGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public Optional<UUID> findShopOwnerId(UUID shopId) {
		return Optional.empty();
	}

	@Override
	public void suspendShop(UUID shopId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}

	@Override
	public void closeShop(UUID shopId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}

	@Override
	public void restoreShop(UUID shopId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}
}
