package com.twohands.notification_service.infrastructure.persistence.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationEventJpaRepository extends JpaRepository<NotificationEventEntity, UUID> {

    Optional<NotificationEventEntity> findBySourceServiceAndSourceEventId(
            NotificationSourceService sourceService,
            UUID sourceEventId
    );

    Optional<NotificationEventEntity> findBySourceServiceAndEventKey(
            NotificationSourceService sourceService,
            String eventKey
    );
}
