package com.twohands.social_service.infrastructure.persistence.adapter;

import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.infrastructure.persistence.jpa.entity.ProcessedDomainEventEntity;
import com.twohands.social_service.infrastructure.persistence.jpa.repository.ProcessedDomainEventJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public class ProcessedDomainEventRepositoryAdapter implements ProcessedDomainEventRepository {

    private final ProcessedDomainEventJpaRepository jpaRepository;

    public ProcessedDomainEventRepositoryAdapter(ProcessedDomainEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return jpaRepository.existsById(eventId);
    }

    @Override
    public void markProcessed(UUID eventId, String consumerName, String eventType) {
        jpaRepository.save(new ProcessedDomainEventEntity(eventId, consumerName, eventType, Instant.now()));
    }
}
