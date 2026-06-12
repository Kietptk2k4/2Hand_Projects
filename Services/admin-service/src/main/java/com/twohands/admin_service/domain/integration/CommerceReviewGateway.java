package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface CommerceReviewGateway {

	boolean isEnabled();

	Optional<CommerceReviewParties> findReviewParties(UUID reviewId);

	void removeReview(UUID reviewId, UUID adminId, String reason);

	void restoreReview(UUID reviewId, UUID adminId, String reason);

	record CommerceReviewParties(UUID reviewAuthorId, UUID sellerUserId) {
	}
}
