package com.twohands.auth_service.domain.session;

import com.twohands.auth_service.domain.session.event.SessionLoggedOutEvent;
import com.twohands.auth_service.domain.session.event.SessionRevokedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RefreshTokenSessionTest {

    @Test
    void shouldAllowRefreshWhenActiveAndNotExpired() {
        Instant now = Instant.now();
        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-hash",
                "device-1",
                "127.0.0.1",
                "UA",
                now.plusSeconds(300),
                now
        );

        session.ensureUsableForRefresh(now.plusSeconds(10));
    }

    @Test
    void shouldRejectRefreshWhenExpired() {
        Instant now = Instant.now();
        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-hash",
                "device-1",
                "127.0.0.1",
                "UA",
                now.minusSeconds(1),
                now.minusSeconds(100)
        );

        SessionDomainError error = assertThrows(SessionDomainError.class, () -> session.ensureUsableForRefresh(now));

        assertEquals("SESSION_EXPIRED", error.code());
    }

    @Test
    void shouldEmitEventOnLogoutAndRevoke() {
        Instant now = Instant.now();
        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-hash",
                "device-1",
                "127.0.0.1",
                "UA",
                now.plusSeconds(600),
                now
        );

        session.logout(now.plusSeconds(10));
        assertEquals(SessionStatus.LOGGED_OUT, session.status());
        assertInstanceOf(SessionLoggedOutEvent.class, session.pullDomainEvents().getFirst());

        session.revoke(now.plusSeconds(20));
        assertEquals(SessionStatus.REVOKED, session.status());
        assertInstanceOf(SessionRevokedEvent.class, session.pullDomainEvents().getFirst());
    }
}
