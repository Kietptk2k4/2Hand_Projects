package com.twohands.notification_service.application.settings;

import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventTypeAliasResolver;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSettingRepository;
import com.twohands.notification_service.exception.AppException;
import com.twohands.notification_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateNotificationSettingsUseCase {

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final NotificationEventTypeAliasResolver notificationEventTypeAliasResolver;

    public UpdateNotificationSettingsUseCase(
            UserNotificationSettingRepository userNotificationSettingRepository,
            NotificationEventTypeAliasResolver notificationEventTypeAliasResolver
    ) {
        this.userNotificationSettingRepository = userNotificationSettingRepository;
        this.notificationEventTypeAliasResolver = notificationEventTypeAliasResolver;
    }

    @Transactional
    public UpdateNotificationSettingsResult execute(UpdateNotificationSettingsCommand command) {
        validateCommand(command);

        String eventType = notificationEventTypeAliasResolver.resolve(command.eventType());
        validateSupportedEventType(eventType);

        UUID userId = command.userId();
        Instant now = Instant.now();
        Optional<UserNotificationSetting> existing =
                userNotificationSettingRepository.findByUserIdAndEventType(userId, eventType);
        Instant createdAt = existing.map(UserNotificationSetting::createdAt).orElse(now);

        UserNotificationSetting saved = userNotificationSettingRepository.save(
                new UserNotificationSetting(
                        userId,
                        eventType,
                        command.allowPush(),
                        command.allowEmail(),
                        command.allowInApp(),
                        createdAt,
                        now
                )
        );

        return new UpdateNotificationSettingsResult(
                saved.userId(),
                saved.eventType(),
                saved.allowPush(),
                saved.allowEmail(),
                saved.allowInApp(),
                saved.updatedAt()
        );
    }

    public String successMessage() {
        return "Notification setting updated successfully";
    }

    private void validateCommand(UpdateNotificationSettingsCommand command) {
        if (command.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        if (command.eventType() == null || command.eventType().isBlank()) {
            throw validationError("eventType", "Event type must not be blank.");
        }
    }

    private void validateSupportedEventType(String eventType) {
        if (!NotificationDefaultChannelPolicy.isKnownEventType(eventType)) {
            throw new AppException(
                    ErrorCode.UNKNOWN_EVENT_TYPE,
                    "Unknown event type for notification settings",
                    "eventType",
                    "Event type is not configured for delivery."
            );
        }
    }

    private AppException validationError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
