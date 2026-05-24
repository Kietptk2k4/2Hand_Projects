package com.twohands.notification_service.domain.notificationsetting;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserNotificationSettingRepository {

    UserNotificationSetting save(UserNotificationSetting setting);

    Optional<UserNotificationSetting> findByUserIdAndEventType(UUID userId, String eventType);

    List<UserNotificationSetting> findByUserId(UUID userId);

    Set<String> findEventTypesByUserId(UUID userId);
}
