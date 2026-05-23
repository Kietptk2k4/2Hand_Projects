package com.twohands.admin_service.constant;

import java.util.Set;

public final class AdminRole {

	public static final String MODERATOR = "MODERATOR";
	public static final String SUPPORT = "SUPPORT";
	public static final String SUPER_ADMIN = "SUPER_ADMIN";

	private static final Set<String> KNOWN_CODES = Set.of(MODERATOR, SUPPORT, SUPER_ADMIN);

	private AdminRole() {
	}

	public static Set<String> knownCodes() {
		return KNOWN_CODES;
	}

	public static boolean isKnown(String roleCode) {
		return roleCode != null && KNOWN_CODES.contains(roleCode);
	}
}
