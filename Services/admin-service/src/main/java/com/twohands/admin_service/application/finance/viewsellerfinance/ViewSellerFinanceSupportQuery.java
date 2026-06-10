package com.twohands.admin_service.application.finance.viewsellerfinance;

import java.util.UUID;

public record ViewSellerFinanceSupportQuery(
		UUID sellerId,
		String from,
		String to,
		Integer page,
		Integer limit,
		String bearerToken
) {
}
