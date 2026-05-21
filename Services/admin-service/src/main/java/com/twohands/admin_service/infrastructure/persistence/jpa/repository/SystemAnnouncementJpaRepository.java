package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemAnnouncementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SystemAnnouncementJpaRepository extends JpaRepository<SystemAnnouncementEntity, UUID> {
}
