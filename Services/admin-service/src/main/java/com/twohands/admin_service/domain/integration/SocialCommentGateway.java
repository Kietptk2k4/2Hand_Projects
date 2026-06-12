package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface SocialCommentGateway {

	boolean isEnabled();

	void ensureCommentExists(String commentId);

	Optional<UUID> findAuthorUserId(String commentId);

	Optional<String> findPostId(String commentId);
}
