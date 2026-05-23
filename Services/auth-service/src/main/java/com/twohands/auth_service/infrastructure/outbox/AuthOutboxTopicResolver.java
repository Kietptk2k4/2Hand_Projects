package com.twohands.auth_service.infrastructure.outbox;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthOutboxTopicResolver {

    private static final Map<String, String> EVENT_TYPE_TO_TOPIC = Map.ofEntries(
            Map.entry("USER_CREATED", "auth.user.created"),
            Map.entry("USER_UPDATED", "auth.user.updated"),
            Map.entry("USER_DELETED", "auth.user.deleted"),
            Map.entry("EMAIL_VERIFICATION_REQUESTED", "auth.email.verification_requested"),
            Map.entry("PASSWORD_RESET_REQUESTED", "auth.password.reset_requested"),
            Map.entry("PASSWORD_CHANGED", "auth.password.changed")
    );

    public String resolve(String eventType) {
        String topic = EVENT_TYPE_TO_TOPIC.get(eventType);
        if (topic == null) {
            throw new AppException(
                    ErrorCode.INTERNAL_ERROR,
                    "Unsupported outbox event type for publish: " + eventType
            );
        }
        return topic;
    }
}
