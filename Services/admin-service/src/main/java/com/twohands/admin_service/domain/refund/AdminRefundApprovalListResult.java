package com.twohands.admin_service.domain.refund;

import java.util.List;

public record AdminRefundApprovalListResult(
		List<AdminRefundApprovalItem> items,
		int page,
		int limit,
		long totalItems,
		int totalPages,
		boolean hasNext
) {
}
