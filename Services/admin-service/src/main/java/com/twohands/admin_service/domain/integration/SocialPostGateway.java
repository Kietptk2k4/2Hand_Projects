package com.twohands.admin_service.domain.integration;

public interface SocialPostGateway {

	boolean isEnabled();

	void ensurePostExists(String postId);
}
