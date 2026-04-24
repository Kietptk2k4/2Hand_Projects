package com.twohands.authservice.infrastructure.persistence.adapter;

import com.twohands.authservice.application.auth.event.OutboxRecord;
import com.twohands.authservice.application.auth.port.OutboxRepository;
import com.twohands.authservice.infrastructure.persistence.entity.OutboxEventEntity;
import com.twohands.authservice.infrastructure.persistence.repository.OutboxEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OutboxRepositoryAdapter implements OutboxRepository {

    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventJpaRepository jpaRepository;

    public OutboxRepositoryAdapter(OutboxEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(OutboxRecord record) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setId(record.id());
        entity.setEventType(record.eventType());
        entity.setSource(record.source());
        entity.setPayload(record.payload());
        entity.setStatus("PENDING");
        entity.setCreatedAt(Instant.now());
        entity.setRetryCount(0);
        jpaRepository.save(entity);
    }

    @Override
    public List<OutboxRecord> findPending() {
        return jpaRepository.findByStatusOrderByCreatedAtAsc("PENDING").stream()
                .filter(e -> e.getRetryCount() < MAX_RETRY_COUNT)
                .map(e -> new OutboxRecord(e.getId(), e.getEventType(), e.getSource(), e.getPayload()))
                .collect(Collectors.toList());
    }

    @Override
    public void markPublished(UUID id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus("PUBLISHED");
            entity.setPublishedAt(Instant.now());
            jpaRepository.save(entity);
        });
    }

    @Override
    public void incrementRetry(UUID id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            int newCount = entity.getRetryCount() + 1;
            entity.setRetryCount(newCount);
            if (newCount >= MAX_RETRY_COUNT) {
                entity.setStatus("FAILED");
            }
            jpaRepository.save(entity);
        });
    }
}
