package com.twohands.admin_service.domain.integration;

import java.util.Optional;
import java.util.UUID;

public interface SocialPostGateway {

	boolean isEnabled();

	void ensurePostExists(String postId);

	Optional<UUID> findAuthorUserId(String postId);
}
