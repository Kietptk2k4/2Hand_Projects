package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
}
