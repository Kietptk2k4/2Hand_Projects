package com.twohands.admin_service.domain.auth;

public interface AuthLoginGateway {

	boolean isEnabled();

	AdminLoginTokens login(AdminCredentialLogin login);
}
