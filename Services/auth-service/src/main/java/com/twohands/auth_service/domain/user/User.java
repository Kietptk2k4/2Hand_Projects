package com.twohands.auth_service.domain.user;

import com.twohands.auth_service.domain.user.event.PasswordChangedEvent;
import com.twohands.auth_service.domain.user.event.UserCreatedEvent;
import com.twohands.auth_service.domain.user.event.UserDeletedEvent;
import com.twohands.auth_service.domain.user.event.UserVerifiedEvent;
import com.twohands.auth_service.domain.shared.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class User {
    private final UUID id;
    private EmailAddress email;
    private String phone;
    private String phoneNormalized;
    private PasswordHash passwordHash;
    private UserStatus status;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Instant lastLoginAt;
    private Instant passwordChangedAt;
    private Instant deletedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    private User(
            UUID id,
            EmailAddress email,
            PasswordHash passwordHash,
            UserStatus status,
            boolean emailVerified,
            boolean phoneVerified,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new UserDomainError("USER_ID_REQUIRED", "User id is required");
        }
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User registerWithEmail(UUID id, EmailAddress email, PasswordHash passwordHash, Instant now) {
        validateRawPasswordStrength(passwordHash.value());
        User user = new User(id, email, passwordHash, UserStatus.PENDING_VERIFICATION, false, false, now, now);
        user.pendingEvents.add(new UserCreatedEvent(id, email.normalizedValue(), UserStatus.PENDING_VERIFICATION, now));
        return user;
    }

    public static User registerWithOAuth(UUID id, EmailAddress email, Instant now) {
        User user = new User(id, email, PasswordHash.of("OAUTH_NO_PASSWORD"), UserStatus.ACTIVE, true, false, now, now);
        user.pendingEvents.add(new UserCreatedEvent(id, email.normalizedValue(), UserStatus.ACTIVE, now));
        return user;
    }

    public void markEmailVerified(Instant now) {
        if (status != UserStatus.PENDING_VERIFICATION) {
            throw new UserDomainError("USER_VERIFY_INVALID_STATUS", "Only pending user can be verified");
        }
        this.emailVerified = true;
        this.status = UserStatus.ACTIVE;
        this.updatedAt = now;
        this.pendingEvents.add(new UserVerifiedEvent(id, now));
    }

    public void markLoginSuccess(Instant now) {
        ensureLoginAllowed();
        this.lastLoginAt = now;
        this.updatedAt = now;
    }

    public void suspend(Instant now) {
        ensureNotDeleted();
        this.status = UserStatus.SUSPENDED;
        this.updatedAt = now;
    }

    public void reactivate(Instant now) {
        ensureNotDeleted();
        this.status = UserStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void changePassword(PasswordHash newPasswordHash, Instant now) {
        ensureNotDeleted();
        validateRawPasswordStrength(newPasswordHash.value());
        if (this.passwordHash.value().equals(newPasswordHash.value())) {
            throw new UserDomainError("USER_PASSWORD_DUPLICATED", "New password must be different from old password");
        }
        this.passwordHash = newPasswordHash;
        this.passwordChangedAt = now;
        this.updatedAt = now;
        this.pendingEvents.add(new PasswordChangedEvent(id, now));
    }

    public void softDelete(Instant now) {
        ensureNotDeleted();
        this.status = UserStatus.DELETED;
        this.deletedAt = now;
        this.updatedAt = now;
        this.pendingEvents.add(new UserDeletedEvent(id, now));
    }

    public void ensureLoginAllowed() {
        if (status == UserStatus.SUSPENDED || status == UserStatus.DELETED) {
            throw new UserDomainError("USER_LOGIN_FORBIDDEN", "Suspended or deleted user cannot login");
        }
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    public UUID id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public UserStatus status() {
        return status;
    }

    public boolean emailVerified() {
        return emailVerified;
    }

    public boolean phoneVerified() {
        return phoneVerified;
    }

    public Instant lastLoginAt() {
        return lastLoginAt;
    }

    public Instant passwordChangedAt() {
        return passwordChangedAt;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    private static void validateRawPasswordStrength(String passwordHashLikeValue) {
        if (passwordHashLikeValue == null || passwordHashLikeValue.length() < 8) {
            throw new UserDomainError("USER_PASSWORD_TOO_WEAK", "Password must be at least 8 characters");
        }
    }

    private void ensureNotDeleted() {
        if (status == UserStatus.DELETED) {
            throw new UserDomainError("USER_ALREADY_DELETED", "Deleted user cannot be modified");
        }
    }
}
