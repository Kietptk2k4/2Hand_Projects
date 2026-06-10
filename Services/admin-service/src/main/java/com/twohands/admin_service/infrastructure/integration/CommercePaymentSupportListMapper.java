package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;

import java.util.List;

final class CommercePaymentSupportListMapper {

	private CommercePaymentSupportListMapper() {
	}

	static PagedResult<PaymentSupportListEntry> toDomain(CommercePaymentsSupportPayload payload) {
		if (payload == null) {
			return new PagedResult<>(List.of(), 1, 20, 0L, 0);
		}
		List<PaymentSupportListEntry> items = payload.payments() == null
				? List.of()
				: payload.payments().stream().map(CommercePaymentSupportListMapper::toEntry).toList();
		return new PagedResult<>(
				items,
				payload.page(),
				payload.size(),
				payload.totalElements(),
				payload.totalPages()
		);
	}

	private static PaymentSupportListEntry toEntry(CommercePaymentsSupportPayload.PaymentListPayload payment) {
		return new PaymentSupportListEntry(
				payment.paymentId(),
				payment.orderId(),
				payment.paymentMethod(),
				payment.amount(),
				payment.currency(),
				payment.status(),
				payment.paidAt(),
				payment.createdAt()
		);
	}
}
