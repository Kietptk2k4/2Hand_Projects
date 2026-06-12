package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceReviewGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceReviewGateway implements CommerceReviewGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public Optional<CommerceReviewParties> findReviewParties(UUID reviewId) {
		return Optional.empty();
	}

	@Override
	public void removeReview(UUID reviewId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}

	@Override
	public void restoreReview(UUID reviewId, UUID adminId, String reason) {
		// No-op when Commerce integration is disabled.
	}
}
