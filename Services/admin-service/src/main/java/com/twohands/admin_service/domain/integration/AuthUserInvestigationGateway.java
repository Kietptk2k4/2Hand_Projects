package com.twohands.admin_service.domain.integration;

import java.util.UUID;

public interface AuthUserInvestigationGateway {

	boolean isEnabled();

	InvestigationUserProfile fetchInvestigationProfile(UUID userId, String bearerToken);
}
