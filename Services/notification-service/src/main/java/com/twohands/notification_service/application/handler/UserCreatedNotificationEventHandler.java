package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsCommand;
import com.twohands.notification_service.application.settings.InitializeDefaultNotificationSettingsUseCase;
import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Order(0)
public class UserCreatedNotificationEventHandler implements NotificationEventHandler {

    private static final String USER_CREATED = "USER_CREATED";

    private final NotificationRecipientResolver recipientResolver;
    private final InitializeDefaultNotificationSettingsUseCase initializeDefaultNotificationSettingsUseCase;

    public UserCreatedNotificationEventHandler(
            NotificationRecipientResolver recipientResolver,
            InitializeDefaultNotificationSettingsUseCase initializeDefaultNotificationSettingsUseCase
    ) {
        this.recipientResolver = recipientResolver;
        this.initializeDefaultNotificationSettingsUseCase = initializeDefaultNotificationSettingsUseCase;
    }

    @Override
    public boolean supports(String eventType) {
        return USER_CREATED.equals(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        UUID userId = resolveUserId(event);
        if (userId == null) {
            return NotificationEventHandlerResult.failure(
                    "User id is required for USER_CREATED notification event",
                    NotificationFailurePolicy.RETRYABLE
            );
        }

        try {
            initializeDefaultNotificationSettingsUseCase.execute(
                    new InitializeDefaultNotificationSettingsCommand(userId)
            );
            return NotificationEventHandlerResult.success();
        } catch (DataAccessException ex) {
            return NotificationEventHandlerResult.failure(
                    "Failed to initialize default notification settings",
                    NotificationFailurePolicy.RETRYABLE
            );
        }
    }

    private UUID resolveUserId(NotificationEvent event) {
        if (event.recipientUserId() != null) {
            return event.recipientUserId();
        }
        if (event.actorId() != null) {
            return event.actorId();
        }

        List<UUID> recipients = recipientResolver.resolve(event);
        return recipients.isEmpty() ? null : recipients.getFirst();
    }
}
