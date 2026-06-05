package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface CommerceReviewGateway {

	boolean isEnabled();

	Optional<CommerceReviewParties> findReviewParties(UUID reviewId);

	record CommerceReviewParties(UUID reviewAuthorId, UUID sellerUserId) {
	}
}
