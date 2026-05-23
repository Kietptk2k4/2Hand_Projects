package com.twohands.admin_service.application.auth.adminlogin;

import com.twohands.admin_service.domain.auth.AdminCredentialLogin;
import com.twohands.admin_service.domain.auth.AdminLoginTokens;
import com.twohands.admin_service.domain.auth.AuthLoginGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminLoginUseCase {

	private static final Logger log = LoggerFactory.getLogger(AdminLoginUseCase.class);
	private static final String SUCCESS_MESSAGE = "Admin login delegated successfully";

	private final AuthLoginGateway authLoginGateway;

	public AdminLoginUseCase(AuthLoginGateway authLoginGateway) {
		this.authLoginGateway = authLoginGateway;
	}

	public AdminLoginResult execute(AdminLoginCommand command) {
		if (!authLoginGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Admin login is owned by Auth Service. Call Auth Service directly or enable admin.integrations.auth."
			);
		}
		log.info("Delegating admin login to Auth Service for email={}", maskEmail(command.email()));
		AdminLoginTokens tokens = authLoginGateway.login(new AdminCredentialLogin(
				command.email(),
				command.password(),
				command.ipAddress(),
				command.userAgent(),
				command.deviceId()
		));
		return new AdminLoginResult(
				tokens.accessToken(),
				tokens.refreshToken(),
				tokens.expiresIn(),
				tokens.adminId(),
				tokens.email(),
				tokens.status(),
				tokens.roles(),
				tokens.permissions()
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return "***";
		}
		int at = email.indexOf('@');
		String local = email.substring(0, at);
		if (local.length() <= 2) {
			return "**" + email.substring(at);
		}
		return local.substring(0, 2) + "***" + email.substring(at);
	}
}
