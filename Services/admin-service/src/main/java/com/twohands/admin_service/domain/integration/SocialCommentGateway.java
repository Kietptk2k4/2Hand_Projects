package com.twohands.admin_service.domain.integration;

public interface SocialCommentGateway {

	boolean isEnabled();

	void ensureCommentExists(String commentId);
}
