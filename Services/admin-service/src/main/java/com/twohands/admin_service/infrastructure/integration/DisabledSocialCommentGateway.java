package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.SocialCommentGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.social.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledSocialCommentGateway implements SocialCommentGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void ensureCommentExists(String commentId) {
		// Comment existence is validated when Social integration is enabled.
	}
}
