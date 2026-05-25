package com.twohands.notification_service.infrastructure.persistence.notificationsetting;

import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class UserNotificationSettingRepositoryAdapter implements UserNotificationSettingRepository {

    private final UserNotificationSettingJpaRepository jpaRepository;

    public UserNotificationSettingRepositoryAdapter(UserNotificationSettingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserNotificationSetting save(UserNotificationSetting setting) {
        UserNotificationSettingEntity entity = jpaRepository
                .findByUserIdAndEventType(setting.userId(), setting.eventType())
                .orElseGet(UserNotificationSettingEntity::new);

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(setting.createdAt());
        }

        entity.setUserId(setting.userId());
        entity.setEventType(setting.eventType());
        entity.setAllowPush(setting.allowPush());
        entity.setAllowEmail(setting.allowEmail());
        entity.setAllowInApp(setting.allowInApp());
        entity.setUpdatedAt(setting.updatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<UserNotificationSetting> findByUserIdAndEventType(UUID userId, String eventType) {
        return jpaRepository.findByUserIdAndEventType(userId, eventType).map(this::toDomain);
    }

    @Override
    public List<UserNotificationSetting> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Set<String> findEventTypesByUserId(UUID userId) {
        return jpaRepository.findEventTypesByUserId(userId);
    }

    private UserNotificationSetting toDomain(UserNotificationSettingEntity entity) {
        return new UserNotificationSetting(
                entity.getUserId(),
                entity.getEventType(),
                entity.isAllowPush(),
                entity.isAllowEmail(),
                entity.isAllowInApp(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
