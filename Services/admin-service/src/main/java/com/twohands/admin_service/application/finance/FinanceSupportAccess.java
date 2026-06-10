package com.twohands.admin_service.application.finance;

import com.twohands.admin_service.domain.integration.CommerceFinanceSupportGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

public final class FinanceSupportAccess {

	private FinanceSupportAccess() {
	}

	public static void requireEnabled(CommerceFinanceSupportGateway gateway) {
		if (!gateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; finance support is unavailable"
			);
		}
	}
}
