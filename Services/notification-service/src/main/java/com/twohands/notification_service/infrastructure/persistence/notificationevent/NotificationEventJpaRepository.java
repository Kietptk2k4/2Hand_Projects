package com.twohands.notification_service.infrastructure.persistence.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
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

    List<NotificationEventEntity> findByStatusAndLockedAtBeforeOrderByLockedAtAsc(
            NotificationEventStatus status,
            Instant lockedBefore,
            org.springframework.data.domain.Pageable pageable
    );
}
