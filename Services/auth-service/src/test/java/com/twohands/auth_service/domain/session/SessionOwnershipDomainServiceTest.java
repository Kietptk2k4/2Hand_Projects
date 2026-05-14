package com.twohands.auth_service.domain.session;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionOwnershipDomainServiceTest {

    private final SessionOwnershipDomainService service = new SessionOwnershipDomainService();

    @Test
    void shouldRejectManagingOtherUserSession() {
        UUID ownerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();

        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                ownerId,
                "token-hash",
                "device-1",
                "127.0.0.1",
                "UA",
                now.plusSeconds(100),
                now
        );

        SessionDomainError error = assertThrows(SessionDomainError.class, () -> service.ensureOwner(actorId, session));

        assertEquals("SESSION_OWNER_FORBIDDEN", error.code());
    }
}
