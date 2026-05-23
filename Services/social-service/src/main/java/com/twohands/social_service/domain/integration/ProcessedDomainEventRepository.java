package com.twohands.social_service.domain.integration;

import java.util.UUID;

public interface ProcessedDomainEventRepository {

    boolean existsByEventId(UUID eventId);

    void markProcessed(UUID eventId, String consumerName, String eventType);
}
