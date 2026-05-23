package com.twohands.admin_service.domain.announcement;

import java.util.Optional;
import java.util.UUID;

public interface SystemAnnouncementRepository {

	SystemAnnouncement save(SystemAnnouncement announcement);

	Optional<SystemAnnouncement> findById(UUID announcementId);
}
