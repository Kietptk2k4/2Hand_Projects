package com.twohands.admin_service.infrastructure.persistence.moderation;

import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.domain.moderation.ContentModerationLogRepository;
import com.twohands.admin_service.domain.moderation.ContentModerationTargetType;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.ContentModerationLogEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.ContentModerationLogJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ContentModerationLogRepositoryAdapter implements ContentModerationLogRepository {

	private final ContentModerationLogJpaRepository jpaRepository;

	public ContentModerationLogRepositoryAdapter(ContentModerationLogJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public ContentModerationLog save(ContentModerationLog log) {
		ContentModerationLogEntity entity = new ContentModerationLogEntity();
		entity.setId(log.id());
		entity.setTargetType(com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationTargetType.valueOf(
				log.targetType().name()));
		entity.setTargetId(log.targetId());
		entity.setAction(com.twohands.admin_service.infrastructure.persistence.jpa.enums.ContentModerationAction.valueOf(
				log.action().name()));
		entity.setReason(log.reason());
		entity.setAdminId(log.adminId());
		entity.setCreatedAt(log.createdAt());
		entity.setNote(log.note());
		return toDomain(jpaRepository.save(entity));
	}

	private ContentModerationLog toDomain(ContentModerationLogEntity entity) {
		return new ContentModerationLog(
				entity.getId(),
				ContentModerationTargetType.valueOf(entity.getTargetType().name()),
				entity.getTargetId(),
				ContentModerationAction.valueOf(entity.getAction().name()),
				entity.getReason(),
				entity.getAdminId(),
				entity.getCreatedAt(),
				entity.getNote()
		);
	}
}
