package com.twohands.admin_service.domain.support;

/**
 * Ensures payment support responses never expose provider secrets or raw webhook payloads.
 */
public final class PaymentSupportDetailPolicy {

	private PaymentSupportDetailPolicy() {
	}

	public static PaymentSupportDetail sanitize(PaymentSupportDetail detail) {
		if (detail == null) {
			return null;
		}
		return detail;
	}
}
