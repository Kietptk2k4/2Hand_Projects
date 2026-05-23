package com.twohands.admin_service.domain.audit;

public interface AdminRequestContextProvider {

	String clientIpAddress();

	String userAgent();
}
