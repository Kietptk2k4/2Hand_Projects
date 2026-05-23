package com.twohands.admin_service.domain.auth;

public interface AuthRefreshGateway {

	boolean isEnabled();

	AdminRefreshedAccessToken refresh(AdminRefreshTokenRequest request);
}
