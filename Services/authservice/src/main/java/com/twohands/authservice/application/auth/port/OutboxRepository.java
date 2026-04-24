package com.twohands.authservice.application.auth.port;

import com.twohands.authservice.application.auth.event.OutboxRecord;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    void save(OutboxRecord record);

    List<OutboxRecord> findPending();

    void markPublished(UUID id);

    void incrementRetry(UUID id);
}
