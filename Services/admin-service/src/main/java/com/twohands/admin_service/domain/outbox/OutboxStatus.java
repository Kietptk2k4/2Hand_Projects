package com.twohands.admin_service.domain.outbox;

public enum OutboxStatus {
	PENDING,
	PROCESSING,
	PUBLISHED,
	FAILED
}
