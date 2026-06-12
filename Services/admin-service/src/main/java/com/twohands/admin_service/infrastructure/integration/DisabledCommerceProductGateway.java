package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceProductGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceProductGateway implements CommerceProductGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void ensureProductExists(UUID productId) {
		// Product existence is validated when Commerce integration is enabled.
	}

	@Override
	public java.util.Optional<UUID> findSellerUserId(UUID productId) {
		return java.util.Optional.empty();
	}

	@Override
	public void removeProduct(UUID productId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}

	@Override
	public void restoreProduct(UUID productId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}
}
