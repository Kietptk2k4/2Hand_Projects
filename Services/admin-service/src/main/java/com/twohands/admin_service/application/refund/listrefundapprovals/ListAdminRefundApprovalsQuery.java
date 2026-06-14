package com.twohands.admin_service.application.refund.listrefundapprovals;

import java.util.Optional;

public record ListAdminRefundApprovalsQuery(
		Optional<String> status,
		Integer page,
		Integer limit,
		String bearerToken
) {
}
