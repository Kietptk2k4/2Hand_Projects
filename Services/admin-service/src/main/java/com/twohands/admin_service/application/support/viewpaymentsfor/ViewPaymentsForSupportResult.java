package com.twohands.admin_service.application.support.viewpaymentsfor;

import com.twohands.admin_service.domain.support.PaymentSupportListEntry;

import java.util.List;

public record ViewPaymentsForSupportResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<PaymentSupportListEntry> payments
) {
}
