package com.twohands.admin_service.application.refund.listrefundapprovals;

import java.util.Optional;

public record ListAdminRefundApprovalsQuery(
		Optional<String> status,
		Optional<String> q,
		Optional<String> requestedBy,
		Optional<String> paymentMethod,
		Optional<String> from,
		Optional<String> to,
		Integer page,
		Integer limit,
		String bearerToken
) {
}
