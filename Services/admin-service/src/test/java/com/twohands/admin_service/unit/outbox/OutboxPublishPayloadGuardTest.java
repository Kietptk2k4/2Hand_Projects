package com.twohands.admin_service.unit.outbox;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.infrastructure.outbox.OutboxPublishPayloadGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutboxPublishPayloadGuardTest {

	private final OutboxPublishPayloadGuard guard = new OutboxPublishPayloadGuard();

	@Test
	void assertSafeToPublish_allowsBusinessPayload() {
		assertDoesNotThrow(() -> guard.assertSafeToPublish("{\"user_id\":\"uuid\",\"reason\":\"abuse\"}"));
	}

	@Test
	void assertSafeToPublish_rejectsPasswordField() {
		assertThrows(AppException.class, () -> guard.assertSafeToPublish("{\"password\":\"secret\"}"));
	}
}
