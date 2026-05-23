package com.twohands.admin_service.domain.integration;

import java.util.UUID;

public interface CommerceProductGateway {

	boolean isEnabled();

	void ensureProductExists(UUID productId);
}
