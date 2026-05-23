package com.twohands.social_service.infrastructure.integration.kafka;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthUserEventTopicResolver {

    private static final Map<String, String> TOPIC_TO_EVENT_TYPE = Map.ofEntries(
            Map.entry("auth.user.created", "USER_CREATED"),
            Map.entry("auth.user.updated", "USER_UPDATED"),
            Map.entry("auth.user.deleted", "USER_DELETED"),
            Map.entry("admin.user.suspended", "USER_SUSPENDED"),
            Map.entry("admin.user.banned", "USER_BANNED"),
            Map.entry("admin.user.restricted", "USER_RESTRICTED"),
            Map.entry("admin.user.enforcement_revoked", "USER_ENFORCEMENT_REVOKED"),
            Map.entry("admin.user.enforcement_expired", "USER_ENFORCEMENT_EXPIRED")
    );

    public String resolveEventType(String topic) {
        return TOPIC_TO_EVENT_TYPE.get(topic);
    }
}
