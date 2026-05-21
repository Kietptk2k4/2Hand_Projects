package com.twohands.notification_service.infrastructure.persistence.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationEventRepositoryAdapter implements NotificationEventRepository {

    private final NotificationEventJpaRepository jpaRepository;

    public NotificationEventRepositoryAdapter(NotificationEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NotificationEvent save(NotificationEvent event) {
        NotificationEventEntity saved = jpaRepository.save(NotificationEventMapper.toEntity(event));
        return NotificationEventMapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationEvent> findBySourceServiceAndSourceEventId(
            NotificationSourceService sourceService,
            UUID sourceEventId
    ) {
        return jpaRepository.findBySourceServiceAndSourceEventId(sourceService, sourceEventId)
                .map(NotificationEventMapper::toDomain);
    }

    @Override
    public Optional<NotificationEvent> findBySourceServiceAndEventKey(
            NotificationSourceService sourceService,
            String eventKey
    ) {
        return jpaRepository.findBySourceServiceAndEventKey(sourceService, eventKey)
                .map(NotificationEventMapper::toDomain);
    }
}
