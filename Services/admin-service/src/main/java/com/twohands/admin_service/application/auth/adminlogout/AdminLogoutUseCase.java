package com.twohands.admin_service.application.auth.adminlogout;

import com.twohands.admin_service.domain.auth.AdminLogoutDelegation;
import com.twohands.admin_service.domain.auth.AuthLogoutGateway;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminLogoutUseCase {

	private static final Logger log = LoggerFactory.getLogger(AdminLogoutUseCase.class);
	private static final String SUCCESS_MESSAGE = "Admin logout delegated successfully";

	private final AuthLogoutGateway authLogoutGateway;
	private final AdminAuthorizationService adminAuthorizationService;

	public AdminLogoutUseCase(
			AuthLogoutGateway authLogoutGateway,
			AdminAuthorizationService adminAuthorizationService
	) {
		this.authLogoutGateway = authLogoutGateway;
		this.adminAuthorizationService = adminAuthorizationService;
	}

	public void execute(AdminLogoutCommand command) {
		if (!authLogoutGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Admin logout is owned by Auth Service. Call Auth Service directly or enable admin.integrations.auth."
			);
		}
		adminAuthorizationService.requireCurrentAdmin();
		log.info("Delegating admin logout to Auth Service for adminId={}", adminAuthorizationService.requireCurrentAdminId());
		authLogoutGateway.logout(new AdminLogoutDelegation(
				command.refreshToken(),
				command.ipAddress(),
				command.bearerToken()
		));
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
