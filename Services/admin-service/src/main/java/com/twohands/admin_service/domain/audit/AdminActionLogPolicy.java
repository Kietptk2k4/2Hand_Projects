package com.twohands.admin_service.domain.audit;

import java.util.Set;

/**
 * Determines when audit payloads are persisted (FR_LogCriticalAdminActionPayload).
 */
public final class AdminActionLogPolicy {

	private static final Set<String> CRITICAL_ACTION_TYPES = Set.of(
			"USER_SUSPEND",
			"USER_BAN",
			"USER_RESTRICT",
			"USER_ENFORCEMENT_REVOKE",
			"PRODUCT_REMOVE",
			"PRODUCT_RESTORE",
			"REVIEW_HIDE",
			"REVIEW_REMOVE",
			"REVIEW_RESTORE",
			"SHOP_SUSPEND",
			"SHOP_CLOSE",
			"SHOP_RESTORE",
			"POST_MODERATE",
			"POST_RESTORE",
			"COMMENT_MODERATE",
			"COMMENT_RESTORE",
			"SYSTEM_CONFIG_CREATE",
			"SYSTEM_CONFIG_UPDATE",
			"SYSTEM_CONFIG_TOGGLE",
			"SYSTEM_ANNOUNCEMENT_PUBLISH",
			"SYSTEM_ANNOUNCEMENT_CANCEL",
			"REFUND_EXECUTE",
			"ADMIN_SESSION_REVOKE",
			"SHIPMENT_STATUS_OVERRIDE"
	);

	private AdminActionLogPolicy() {
	}

	public static boolean isCriticalAction(String actionType) {
		return actionType != null && CRITICAL_ACTION_TYPES.contains(actionType);
	}
}
