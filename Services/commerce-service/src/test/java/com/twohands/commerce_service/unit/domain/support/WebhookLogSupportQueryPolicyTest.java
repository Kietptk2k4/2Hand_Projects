package com.twohands.commerce_service.unit.domain.support;

import com.twohands.commerce_service.domain.support.WebhookLogSupportQueryPolicy;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WebhookLogSupportQueryPolicyTest {

	@Test
	void normalizeProvider_acceptsPayosAndGhn() {
		assertEquals("PAYOS", WebhookLogSupportQueryPolicy.normalizeProvider("payos"));
		assertEquals("GHN", WebhookLogSupportQueryPolicy.normalizeProvider("GHN"));
	}

	@Test
	void normalizeProvider_rejectsUnknownProvider() {
		AppException ex = assertThrows(AppException.class, () -> WebhookLogSupportQueryPolicy.normalizeProvider("STRIPE"));
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}

	@Test
	void validateDateRange_rejectsInvalidRange() {
		Instant from = Instant.parse("2026-05-21T00:00:00Z");
		Instant to = Instant.parse("2026-05-20T00:00:00Z");
		AppException ex = assertThrows(
				AppException.class,
				() -> WebhookLogSupportQueryPolicy.validateDateRange(from, to)
		);
		assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
	}
}
