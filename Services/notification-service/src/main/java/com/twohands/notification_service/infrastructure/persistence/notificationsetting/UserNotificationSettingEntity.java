package com.twohands.notification_service.infrastructure.persistence.notificationsetting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_notification_settings")
@IdClass(UserNotificationSettingEntityId.class)
public class UserNotificationSettingEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "allow_push", nullable = false)
    private boolean allowPush;

    @Column(name = "allow_email", nullable = false)
    private boolean allowEmail;

    @Column(name = "allow_in_app", nullable = false)
    private boolean allowInApp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public boolean isAllowPush() {
        return allowPush;
    }

    public void setAllowPush(boolean allowPush) {
        this.allowPush = allowPush;
    }

    public boolean isAllowEmail() {
        return allowEmail;
    }

    public void setAllowEmail(boolean allowEmail) {
        this.allowEmail = allowEmail;
    }

    public boolean isAllowInApp() {
        return allowInApp;
    }

    public void setAllowInApp(boolean allowInApp) {
        this.allowInApp = allowInApp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
