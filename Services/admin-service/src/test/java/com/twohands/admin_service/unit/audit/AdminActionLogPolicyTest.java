package com.twohands.admin_service.unit.audit;

import com.twohands.admin_service.domain.audit.AdminActionLogPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminActionLogPolicyTest {

	@Test
	void isCriticalAction_includesFinancePayoutAndRefundRejectActions() {
		assertTrue(AdminActionLogPolicy.isCriticalAction("PAYOUT_REQUEST_APPROVE"));
		assertTrue(AdminActionLogPolicy.isCriticalAction("PAYOUT_REQUEST_REJECT"));
		assertTrue(AdminActionLogPolicy.isCriticalAction("PAYOUT_REQUEST_MARK_PAID"));
		assertTrue(AdminActionLogPolicy.isCriticalAction("REFUND_REQUEST_REJECT"));
	}

	@Test
	void isCriticalAction_excludesReadOnlySupportViews() {
		assertFalse(AdminActionLogPolicy.isCriticalAction("PAYOUT_SUPPORT_VIEW"));
		assertFalse(AdminActionLogPolicy.isCriticalAction("REFUND_SUPPORT_VIEW"));
	}
}
