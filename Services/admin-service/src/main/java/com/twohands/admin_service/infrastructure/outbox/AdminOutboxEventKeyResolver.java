package com.twohands.admin_service.infrastructure.outbox;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminOutboxEventKeyResolver {

	public String resolve(String eventType, UUID aggregateId) {
		String normalizedType = eventType == null ? "unknown" : eventType.trim().toLowerCase().replace('_', '.');
		return "admin." + normalizedType + ":" + aggregateId;
	}
}
