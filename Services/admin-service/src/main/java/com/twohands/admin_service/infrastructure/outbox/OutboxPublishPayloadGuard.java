package com.twohands.admin_service.infrastructure.outbox;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class OutboxPublishPayloadGuard {

	private static final Set<String> FORBIDDEN_SUBSTRINGS = Set.of(
			"\"password\"",
			"\"token\"",
			"\"secret\"",
			"\"authorization\"",
			"\"otp\"",
			"\"refresh_token\"",
			"\"access_token\""
	);
	private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
			"\"(password|token|secret|authorization|otp|refresh_token|access_token)\"\\s*:",
			Pattern.CASE_INSENSITIVE
	);

	public void assertSafeToPublish(String payloadJson) {
		if (payloadJson == null || payloadJson.isBlank()) {
			return;
		}
		String lower = payloadJson.toLowerCase();
		for (String forbidden : FORBIDDEN_SUBSTRINGS) {
			if (lower.contains(forbidden)) {
				throw new AppException(
						ErrorCode.BAD_REQUEST,
						"Outbox payload contains sensitive field and cannot be published"
				);
			}
		}
		if (FORBIDDEN_PATTERN.matcher(payloadJson).find()) {
			throw new AppException(
					ErrorCode.BAD_REQUEST,
					"Outbox payload contains sensitive field and cannot be published"
			);
		}
	}
}
