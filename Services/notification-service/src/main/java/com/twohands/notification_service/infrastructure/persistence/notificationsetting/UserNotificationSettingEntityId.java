package com.twohands.notification_service.infrastructure.persistence.notificationsetting;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UserNotificationSettingEntityId implements Serializable {

    private UUID userId;
    private String eventType;

    public UserNotificationSettingEntityId() {
    }

    public UserNotificationSettingEntityId(UUID userId, String eventType) {
        this.userId = userId;
        this.eventType = eventType;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserNotificationSettingEntityId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(eventType, that.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, eventType);
    }
}
