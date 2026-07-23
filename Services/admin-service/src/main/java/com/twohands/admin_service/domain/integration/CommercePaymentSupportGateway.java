package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;

import java.util.UUID;

public interface CommercePaymentSupportGateway {

	boolean isEnabled();

	PaymentSupportDetail fetchPaymentSupportDetail(UUID paymentId, String bearerToken);

	PagedResult<PaymentSupportListEntry> searchPayments(
			String status,
			String paymentMethod,
			String orderId,
			String q,
			String reconciliationStatus,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	);
}
