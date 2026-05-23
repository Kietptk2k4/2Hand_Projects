package com.twohands.commerce_service.unit.domain.support;

import com.twohands.commerce_service.domain.support.WebhookPayloadSanitizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WebhookPayloadSanitizerTest {

	@Test
	void sanitizePayos_omitsSignatureAndKeepsSafeFields() {
		String payload = """
				{
				  "code": "00",
				  "desc": "success",
				  "success": true,
				  "signature": "secret-signature",
				  "data": {
				    "orderCode": "PAYOS-123",
				    "amount": 150000
				  }
				}
				""";

		Map<String, Object> summary = WebhookPayloadSanitizer.sanitize("PAYOS", payload);

		assertEquals("00", summary.get("code"));
		assertEquals("PAYOS-123", summary.get("order_code"));
		assertFalse(summary.containsKey("signature"));
	}

	@Test
	void sanitizeGhn_keepsCarrierStatusOnly() {
		String payload = """
				{
				  "OrderCode": "GHN-999",
				  "Status": "delivered",
				  "ClientOrderCode": "ORDER-1"
				}
				""";

		Map<String, Object> summary = WebhookPayloadSanitizer.sanitize("GHN", payload);

		assertEquals("GHN-999", summary.get("order_code"));
		assertEquals("delivered", summary.get("status"));
		assertFalse(summary.containsKey("ClientOrderCode"));
	}
}
