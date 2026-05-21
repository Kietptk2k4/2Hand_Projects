package com.twohands.admin_service.infrastructure.persistence.jpa.enums;

public enum OutboxStatusType {
	PENDING,
	PROCESSING,
	PUBLISHED,
	FAILED
}
