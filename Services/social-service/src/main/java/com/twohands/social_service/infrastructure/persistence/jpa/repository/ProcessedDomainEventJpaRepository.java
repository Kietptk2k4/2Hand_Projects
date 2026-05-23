package com.twohands.social_service.infrastructure.persistence.jpa.repository;

import com.twohands.social_service.infrastructure.persistence.jpa.entity.ProcessedDomainEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedDomainEventJpaRepository extends JpaRepository<ProcessedDomainEventEntity, UUID> {
}
