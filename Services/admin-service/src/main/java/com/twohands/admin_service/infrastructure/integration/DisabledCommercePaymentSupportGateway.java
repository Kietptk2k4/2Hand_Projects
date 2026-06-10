package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommercePaymentSupportGateway implements CommercePaymentSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public PagedResult<PaymentSupportListEntry> searchPayments(
			String status,
			String paymentMethod,
			String orderId,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; payment support list is unavailable"
		);
	}

	@Override
	public PaymentSupportDetail fetchPaymentSupportDetail(UUID paymentId, String bearerToken) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; payment support detail is unavailable"
		);
	}
}
