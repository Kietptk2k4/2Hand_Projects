package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceRefundSupportGateway;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceRefundSupportGateway implements CommerceRefundSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	private AppException disabled() {
		return new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; refund approvals are unavailable"
		);
	}

	@Override
	public AdminRefundApprovalListResult listRefundApprovals(
			Optional<String> status,
			Integer page,
			Integer limit,
			String bearerToken
	) {
		throw disabled();
	}

	@Override
	public AdminRefundApprovalItem getRefundApproval(UUID refundRequestId, String bearerToken) {
		throw disabled();
	}

	@Override
	public AdminRefundApprovalItem confirmRefundApproval(UUID refundRequestId, String adminNote, String bearerToken) {
		throw disabled();
	}

	@Override
	public AdminRefundApprovalItem rejectRefundApproval(UUID refundRequestId, String adminNote, String bearerToken) {
		throw disabled();
	}
}
