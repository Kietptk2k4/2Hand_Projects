package com.twohands.admin_service.domain.auth;

public interface AuthLogoutGateway {

	boolean isEnabled();

	void logout(AdminLogoutDelegation delegation);
}
