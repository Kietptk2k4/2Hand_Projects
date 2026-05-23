package com.twohands.admin_service.infrastructure.persistence.jpa.repository;

import com.twohands.admin_service.infrastructure.persistence.jpa.entity.ContentModerationLogEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContentModerationLogJpaRepository extends JpaRepository<ContentModerationLogEntity, UUID> {

	Page<ContentModerationLogEntity> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
			ContentModerationTargetType targetType,
			String targetId,
			Pageable pageable
	);
}
