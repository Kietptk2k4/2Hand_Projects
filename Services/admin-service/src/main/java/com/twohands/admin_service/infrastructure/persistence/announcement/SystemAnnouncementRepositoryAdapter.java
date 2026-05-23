package com.twohands.admin_service.infrastructure.persistence.announcement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementRepository;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.entity.SystemAnnouncementEntity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementSeverity;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AnnouncementStatus;
import com.twohands.admin_service.infrastructure.persistence.jpa.repository.SystemAnnouncementJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SystemAnnouncementRepositoryAdapter implements SystemAnnouncementRepository {

	private final SystemAnnouncementJpaRepository jpaRepository;

	public SystemAnnouncementRepositoryAdapter(SystemAnnouncementJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public SystemAnnouncement save(SystemAnnouncement announcement) {
		return toDomain(jpaRepository.save(toEntity(announcement)));
	}

	@Override
	public Optional<SystemAnnouncement> findById(UUID announcementId) {
		return jpaRepository.findById(announcementId).map(this::toDomain);
	}

	private SystemAnnouncementEntity toEntity(SystemAnnouncement announcement) {
		SystemAnnouncementEntity entity = new SystemAnnouncementEntity();
		entity.setId(announcement.id());
		entity.setTitle(announcement.title());
		entity.setContent(announcement.content());
		entity.setSeverity(AnnouncementSeverity.valueOf(announcement.severity().name()));
		entity.setPinned(announcement.pinned());
		entity.setDismissible(announcement.dismissible());
		entity.setStatus(AnnouncementStatus.valueOf(announcement.status().name()));
		entity.setCreatedBy(announcement.createdBy());
		entity.setCreatedAt(announcement.createdAt());
		entity.setSentAt(announcement.sentAt());
		return entity;
	}

	private SystemAnnouncement toDomain(SystemAnnouncementEntity entity) {
		return new SystemAnnouncement(
				entity.getId(),
				entity.getTitle(),
				entity.getContent(),
				SystemAnnouncementSeverity.valueOf(entity.getSeverity().name()),
				entity.isPinned(),
				entity.isDismissible(),
				SystemAnnouncementStatus.valueOf(entity.getStatus().name()),
				entity.getCreatedBy(),
				entity.getCreatedAt(),
				entity.getSentAt()
		);
	}
}
