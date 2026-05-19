package com.twohands.social_service.domain.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);

    List<OutboxEvent> claimPublishCandidates(int batchSize, int maxRetries);

    int markPublished(UUID eventId, Instant publishedAt);

    int markFailed(UUID eventId, String lastError);
}
