package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.SocialPostGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.social.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledSocialPostGateway implements SocialPostGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void ensurePostExists(String postId) {
		// Post existence is validated when Social integration is enabled.
	}
}
