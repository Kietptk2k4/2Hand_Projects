package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;

import java.util.Optional;
import java.util.UUID;

public interface CommerceRefundSupportGateway {

	boolean isEnabled();

	AdminRefundApprovalListResult listRefundApprovals(
			Optional<String> status,
			Integer page,
			Integer limit,
			String bearerToken
	);

	AdminRefundApprovalItem confirmRefundApproval(UUID refundRequestId, String adminNote, String bearerToken);

	AdminRefundApprovalItem rejectRefundApproval(UUID refundRequestId, String adminNote, String bearerToken);
}
