package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.AdminActionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminActionLogJpaRepository extends JpaRepository<AdminActionLogEntity, UUID> {
}
