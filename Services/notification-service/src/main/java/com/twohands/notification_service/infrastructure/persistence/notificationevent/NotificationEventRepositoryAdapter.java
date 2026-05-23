package com.twohands.notification_service.infrastructure.persistence.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationEventRepositoryAdapter implements NotificationEventRepository {

    private final NotificationEventJpaRepository jpaRepository;

    public NotificationEventRepositoryAdapter(NotificationEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<NotificationEvent> findById(UUID id) {
        return jpaRepository.findById(id).map(NotificationEventMapper::toDomain);
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

    @Override
    public List<NotificationEvent> findStaleProcessingEvents(Instant lockedBefore, int limit) {
        return jpaRepository.findByStatusAndLockedAtBeforeOrderByLockedAtAsc(
                        NotificationEventStatus.PROCESSING,
                        lockedBefore,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(NotificationEventMapper::toDomain)
                .toList();
    }
}
