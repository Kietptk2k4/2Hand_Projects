package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommercePayoutSupportGateway;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommercePayoutSupportGateway implements CommercePayoutSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	private AppException disabled() {
		return new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; payout support is unavailable"
		);
	}

	@Override
	public AdminPayoutRequestListResult listPayoutRequests(
			Optional<String> status,
			Integer page,
			Integer limit,
			String bearerToken
	) {
		throw disabled();
	}

	@Override
	public AdminPayoutRequestItem approvePayoutRequest(UUID payoutRequestId, String bearerToken) {
		throw disabled();
	}

	@Override
	public AdminPayoutRequestItem rejectPayoutRequest(UUID payoutRequestId, String adminNote, String bearerToken) {
		throw disabled();
	}

	@Override
	public AdminPayoutRequestItem markPayoutRequestPaid(
			UUID payoutRequestId,
			String bankTransferRef,
			String bearerToken
	) {
		throw disabled();
	}
}
