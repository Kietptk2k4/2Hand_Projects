package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.AuthUserInvestigationGateway;
import com.twohands.admin_service.domain.integration.InvestigationUserProfile;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuthUserInvestigationGateway implements AuthUserInvestigationGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public InvestigationUserProfile fetchInvestigationProfile(UUID userId, String bearerToken) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Auth integration is disabled; user investigation profile is unavailable"
		);
	}
}
