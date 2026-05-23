package com.twohands.admin_service.constant;

import java.util.Set;

/**
 * Admin permission codes aligned with Auth RBAC and admin-service-spec.
 * Destructive actions must check explicit permission — not role alone.
 */
public final class AdminPermission {

	public static final String USER_SUSPEND = "USER_SUSPEND";
	public static final String USER_RESTRICT = "USER_RESTRICT";
	public static final String PRODUCT_REMOVE = "PRODUCT_REMOVE";
	public static final String REVIEW_HIDE = "REVIEW_HIDE";
	public static final String SHOP_SUSPEND = "SHOP_SUSPEND";
	public static final String POST_MODERATE = "POST_MODERATE";
	public static final String COMMENT_MODERATE = "COMMENT_MODERATE";
	public static final String SYSTEM_CONFIG_UPDATE = "SYSTEM_CONFIG_UPDATE";
	public static final String SYSTEM_ANNOUNCEMENT_PUBLISH = "SYSTEM_ANNOUNCEMENT_PUBLISH";
	public static final String ADMIN_AUDIT_READ = "ADMIN_AUDIT_READ";
	public static final String ORDER_SUPPORT_READ = "ORDER_SUPPORT_READ";

	private static final Set<String> KNOWN_CODES = Set.of(
			USER_SUSPEND,
			USER_RESTRICT,
			PRODUCT_REMOVE,
			REVIEW_HIDE,
			SHOP_SUSPEND,
			POST_MODERATE,
			COMMENT_MODERATE,
			SYSTEM_CONFIG_UPDATE,
			SYSTEM_ANNOUNCEMENT_PUBLISH,
			ADMIN_AUDIT_READ,
			ORDER_SUPPORT_READ
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
