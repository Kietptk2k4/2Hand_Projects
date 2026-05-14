package com.twohands.auth_service.domain.user;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VerificationTokenTest {

    @Test
    void shouldMarkTokenUsedWhenValid() {
        Instant now = Instant.now();
        VerificationToken token = new VerificationToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-hash",
                VerificationTokenType.EMAIL_VERIFY,
                now.plusSeconds(60),
                null,
                now
        );

        token.markUsed(now.plusSeconds(10));

        assertEquals(now.plusSeconds(10), token.usedAt());
    }

    @Test
    void shouldRejectExpiredTokenUsage() {
        Instant now = Instant.now();
        VerificationToken token = new VerificationToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "token-hash",
                VerificationTokenType.PASSWORD_RESET,
                now.minusSeconds(1),
                null,
                now.minusSeconds(100)
        );

        UserDomainError error = assertThrows(UserDomainError.class, () -> token.markUsed(now));

        assertEquals("USER_VERIFICATION_TOKEN_EXPIRED", error.code());
    }
}
