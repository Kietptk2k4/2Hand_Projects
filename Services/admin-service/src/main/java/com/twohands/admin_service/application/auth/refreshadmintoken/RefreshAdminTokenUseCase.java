package com.twohands.admin_service.application.auth.refreshadmintoken;

import com.twohands.admin_service.domain.auth.AdminRefreshTokenRequest;
import com.twohands.admin_service.domain.auth.AdminRefreshedAccessToken;
import com.twohands.admin_service.domain.auth.AuthRefreshGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RefreshAdminTokenUseCase {

	private static final Logger log = LoggerFactory.getLogger(RefreshAdminTokenUseCase.class);
	private static final String SUCCESS_MESSAGE = "Admin token refresh delegated successfully";

	private final AuthRefreshGateway authRefreshGateway;

	public RefreshAdminTokenUseCase(AuthRefreshGateway authRefreshGateway) {
		this.authRefreshGateway = authRefreshGateway;
	}

	public RefreshAdminTokenResult execute(RefreshAdminTokenCommand command) {
		if (!authRefreshGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Admin token refresh is owned by Auth Service. Call Auth Service directly or enable admin.integrations.auth."
			);
		}
		log.info("Delegating admin token refresh to Auth Service");
		AdminRefreshedAccessToken refreshed = authRefreshGateway.refresh(new AdminRefreshTokenRequest(
				command.refreshToken(),
				command.ipAddress()
		));
		return new RefreshAdminTokenResult(
				refreshed.accessToken(),
				refreshed.expiresIn(),
				refreshed.adminId(),
				refreshed.email(),
				refreshed.status(),
				refreshed.roles(),
				refreshed.permissions()
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
