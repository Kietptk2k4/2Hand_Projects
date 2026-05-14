package com.twohands.auth_service.domain.user;

import com.twohands.auth_service.domain.user.event.PasswordChangedEvent;
import com.twohands.auth_service.domain.user.event.UserCreatedEvent;
import com.twohands.auth_service.domain.user.event.UserDeletedEvent;
import com.twohands.auth_service.domain.user.event.UserVerifiedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDomainTest {

    @Test
    void registerWithEmailShouldStartPendingAndEmitUserCreated() {
        Instant now = Instant.now();
        User user = User.registerWithEmail(
                UUID.randomUUID(),
                EmailAddress.of("test@example.com"),
                PasswordHash.of("hashedPassword123"),
                now
        );

        assertEquals(UserStatus.PENDING_VERIFICATION, user.status());
        assertTrue(user.pullDomainEvents().getFirst() instanceof UserCreatedEvent);
    }

    @Test
    void verifyEmailShouldActivateAndEmitEvent() {
        Instant now = Instant.now();
        User user = User.registerWithEmail(
                UUID.randomUUID(),
                EmailAddress.of("verify@example.com"),
                PasswordHash.of("hashedPassword123"),
                now
        );
        user.pullDomainEvents();

        user.markEmailVerified(now.plusSeconds(10));

        assertEquals(UserStatus.ACTIVE, user.status());
        assertTrue(user.emailVerified());
        assertTrue(user.pullDomainEvents().getFirst() instanceof UserVerifiedEvent);
    }

    @Test
    void loginShouldBeForbiddenWhenSuspended() {
        Instant now = Instant.now();
        User user = User.registerWithEmail(
                UUID.randomUUID(),
                EmailAddress.of("suspend@example.com"),
                PasswordHash.of("hashedPassword123"),
                now
        );
        user.suspend(now.plusSeconds(30));

        UserDomainError error = assertThrows(UserDomainError.class, user::ensureLoginAllowed);
        assertEquals("USER_LOGIN_FORBIDDEN", error.code());
    }

    @Test
    void changePasswordShouldRejectSameHash() {
        Instant now = Instant.now();
        User user = User.registerWithEmail(
                UUID.randomUUID(),
                EmailAddress.of("pass@example.com"),
                PasswordHash.of("hashedPassword123"),
                now
        );

        UserDomainError error = assertThrows(
                UserDomainError.class,
                () -> user.changePassword(PasswordHash.of("hashedPassword123"), now.plusSeconds(1))
        );

        assertEquals("USER_PASSWORD_DUPLICATED", error.code());
    }

    @Test
    void changePasswordShouldEmitPasswordChangedEvent() {
        Instant now = Instant.now();
        User user = User.registerWithEmail(
                UUID.randomUUID(),
                EmailAddress.of("pass2@example.com"),
                PasswordHash.of("oldHash123"),
                now
        );
        user.pullDomainEvents();

        user.changePassword(PasswordHash.of("newHash123"), now.plusSeconds(5));

        List<?> events = user.pullDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(PasswordChangedEvent.class, events.getFirst());
    }

    @Test
    void softDeleteShouldEmitDeletedEvent() {
        Instant now = Instant.now();
        User user = User.registerWithEmail(
                UUID.randomUUID(),
                EmailAddress.of("delete@example.com"),
                PasswordHash.of("deleteHash123"),
                now
        );
        user.pullDomainEvents();

        user.softDelete(now.plusSeconds(20));

        assertEquals(UserStatus.DELETED, user.status());
        assertTrue(user.pullDomainEvents().getFirst() instanceof UserDeletedEvent);
    }
}
