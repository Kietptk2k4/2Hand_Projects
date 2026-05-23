package com.twohands.notification_service.application.settings;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class InitializeDefaultNotificationSettingsUseCase {

    private final UserNotificationSettingRepository userNotificationSettingRepository;

    public InitializeDefaultNotificationSettingsUseCase(
            UserNotificationSettingRepository userNotificationSettingRepository
    ) {
        this.userNotificationSettingRepository = userNotificationSettingRepository;
    }

    @Transactional
    public InitializeDefaultNotificationSettingsResult execute(InitializeDefaultNotificationSettingsCommand command) {
        validateCommand(command);

        UUID userId = command.userId();
        Set<String> existingEventTypes = userNotificationSettingRepository.findEventTypesByUserId(userId);
        Instant now = Instant.now();

        int createdCount = 0;
        int skippedCount = 0;

        for (String eventType : NotificationDefaultChannelPolicy.supportedEventTypes()) {
            if (existingEventTypes.contains(eventType)) {
                skippedCount++;
                continue;
            }

            DefaultChannelFlags defaults = NotificationDefaultChannelPolicy.resolve(eventType).orElseThrow();
            if (saveDefaultSetting(userId, eventType, defaults, now)) {
                createdCount++;
            } else {
                skippedCount++;
            }
        }

        return new InitializeDefaultNotificationSettingsResult(userId, createdCount, skippedCount);
    }

    private boolean saveDefaultSetting(
            UUID userId,
            String eventType,
            DefaultChannelFlags defaults,
            Instant now
    ) {
        UserNotificationSetting setting = new UserNotificationSetting(
                userId,
                eventType,
                defaults.push(),
                defaults.email(),
                defaults.inApp(),
                now,
                now
        );

        try {
            userNotificationSettingRepository.save(setting);
            return true;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    private void validateCommand(InitializeDefaultNotificationSettingsCommand command) {
        if (command.userId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "userId",
                    "User id is required."
            );
        }
    }
}
