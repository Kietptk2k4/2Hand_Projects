package com.twohands.admin_service.application.finance.viewplatformfinance;

public record ViewPlatformFinanceSupportQuery(
		String from,
		String to,
		String granularity,
		Integer limit,
		String bearerToken
) {
}
