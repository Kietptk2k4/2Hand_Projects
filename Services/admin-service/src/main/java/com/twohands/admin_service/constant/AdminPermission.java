package com.twohands.admin_service.constant;

import java.util.Set;

/**
 * Admin permission codes aligned with Auth RBAC and admin-service-spec.
 * Destructive actions must check explicit permission — not role alone.
 */
public final class AdminPermission {

	public static final String USER_SUSPEND = "USER_SUSPEND";
	public static final String USER_BAN = "USER_BAN";
	public static final String USER_RESTRICT = "USER_RESTRICT";
	public static final String USER_ENFORCEMENT_REVOKE = "USER_ENFORCEMENT_REVOKE";
	public static final String USER_ENFORCEMENT_READ = "USER_ENFORCEMENT_READ";
	public static final String USER_INVESTIGATION_READ = "USER_INVESTIGATION_READ";
	public static final String PRODUCT_REMOVE = "PRODUCT_REMOVE";
	public static final String PRODUCT_RESTORE = "PRODUCT_RESTORE";
	public static final String PRODUCT_MODERATION_READ = "PRODUCT_MODERATION_READ";
	public static final String REVIEW_HIDE = "REVIEW_HIDE";
	public static final String REVIEW_REMOVE = "REVIEW_REMOVE";
	public static final String REVIEW_RESTORE = "REVIEW_RESTORE";
	public static final String REVIEW_MODERATION_READ = "REVIEW_MODERATION_READ";
	public static final String SHOP_SUSPEND = "SHOP_SUSPEND";
	public static final String SHOP_CLOSE = "SHOP_CLOSE";
	public static final String SHOP_RESTORE = "SHOP_RESTORE";
	public static final String SHOP_MODERATION_READ = "SHOP_MODERATION_READ";
	public static final String POST_MODERATE = "POST_MODERATE";
	public static final String POST_RESTORE = "POST_RESTORE";
	public static final String POST_MODERATION_READ = "POST_MODERATION_READ";
	public static final String COMMENT_MODERATE = "COMMENT_MODERATE";
	public static final String COMMENT_RESTORE = "COMMENT_RESTORE";
	public static final String COMMENT_MODERATION_READ = "COMMENT_MODERATION_READ";
	public static final String SYSTEM_CONFIG_UPDATE = "SYSTEM_CONFIG_UPDATE";
	public static final String SYSTEM_CONFIG_VIEW = "SYSTEM_CONFIG_VIEW";
	public static final String SYSTEM_ANNOUNCEMENT_CREATE = "SYSTEM_ANNOUNCEMENT_CREATE";
	public static final String SYSTEM_ANNOUNCEMENT_UPDATE = "SYSTEM_ANNOUNCEMENT_UPDATE";
	public static final String SYSTEM_ANNOUNCEMENT_PUBLISH = "SYSTEM_ANNOUNCEMENT_PUBLISH";
	public static final String SYSTEM_ANNOUNCEMENT_CANCEL = "SYSTEM_ANNOUNCEMENT_CANCEL";
	public static final String ADMIN_AUDIT_READ = "ADMIN_AUDIT_READ";
	public static final String ADMIN_AUDIT_VIEW = "ADMIN_AUDIT_VIEW";
	public static final String ADMIN_SESSION_REVOKE = "ADMIN_SESSION_REVOKE";
	public static final String ORDER_SUPPORT_READ = "ORDER_SUPPORT_READ";
	public static final String PAYMENT_SUPPORT_READ = "PAYMENT_SUPPORT_READ";
	public static final String SHIPMENT_SUPPORT_READ = "SHIPMENT_SUPPORT_READ";
	public static final String SHIPMENT_SUPPORT_WRITE = "SHIPMENT_SUPPORT_WRITE";
	public static final String SHIPMENT_SUPPORT_FORCE_WRITE = "SHIPMENT_SUPPORT_FORCE_WRITE";
	public static final String WEBHOOK_SUPPORT_READ = "WEBHOOK_SUPPORT_READ";
	public static final String PAYOUT_SUPPORT_READ = "PAYOUT_SUPPORT_READ";
	public static final String PAYOUT_SUPPORT_APPROVE = "PAYOUT_SUPPORT_APPROVE";
	public static final String REFUND_SUPPORT_READ = "REFUND_SUPPORT_READ";
	public static final String REFUND_SUPPORT_APPROVE = "REFUND_SUPPORT_APPROVE";
	public static final String FINANCE_SUPPORT_READ = "FINANCE_SUPPORT_READ";
	public static final String CATALOG_READ = "CATALOG_READ";
	public static final String CATALOG_WRITE = "CATALOG_WRITE";

	private static final Set<String> KNOWN_CODES = Set.of(
			USER_SUSPEND,
			USER_BAN,
			USER_RESTRICT,
			USER_ENFORCEMENT_REVOKE,
			USER_ENFORCEMENT_READ,
			USER_INVESTIGATION_READ,
			PRODUCT_REMOVE,
			PRODUCT_RESTORE,
			PRODUCT_MODERATION_READ,
			REVIEW_HIDE,
			REVIEW_REMOVE,
			REVIEW_RESTORE,
			REVIEW_MODERATION_READ,
			SHOP_SUSPEND,
			SHOP_CLOSE,
			SHOP_RESTORE,
			SHOP_MODERATION_READ,
			POST_MODERATE,
			POST_RESTORE,
			POST_MODERATION_READ,
			COMMENT_MODERATE,
			COMMENT_RESTORE,
			COMMENT_MODERATION_READ,
			SYSTEM_CONFIG_UPDATE,
			SYSTEM_CONFIG_VIEW,
			SYSTEM_ANNOUNCEMENT_CREATE,
			SYSTEM_ANNOUNCEMENT_UPDATE,
			SYSTEM_ANNOUNCEMENT_PUBLISH,
			SYSTEM_ANNOUNCEMENT_CANCEL,
			ADMIN_AUDIT_READ,
			ADMIN_AUDIT_VIEW,
			ADMIN_SESSION_REVOKE,
			ORDER_SUPPORT_READ,
			PAYMENT_SUPPORT_READ,
			SHIPMENT_SUPPORT_READ,
			SHIPMENT_SUPPORT_WRITE,
			SHIPMENT_SUPPORT_FORCE_WRITE,
			WEBHOOK_SUPPORT_READ,
			PAYOUT_SUPPORT_READ,
			PAYOUT_SUPPORT_APPROVE,
			REFUND_SUPPORT_READ,
			REFUND_SUPPORT_APPROVE,
			FINANCE_SUPPORT_READ,
			CATALOG_READ,
			CATALOG_WRITE
	);

	private AdminPermission() {
	}

	public static Set<String> knownCodes() {
		return KNOWN_CODES;
	}

	public static boolean isKnown(String permissionCode) {
		return permissionCode != null && KNOWN_CODES.contains(permissionCode);
	}
}
