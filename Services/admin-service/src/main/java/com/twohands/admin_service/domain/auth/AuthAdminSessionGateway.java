package com.twohands.admin_service.domain.auth;

public interface AuthAdminSessionGateway {

	boolean isEnabled();

	AdminSessionRevokeResult revoke(AdminSessionRevokeRequest request);
}
