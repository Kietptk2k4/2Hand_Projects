package com.twohands.auth_service.unit.application.auth.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.auth_service.application.auth.common.EmailVerificationOutboxService;
import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.domain.user.VerificationTokenType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailVerificationOutboxServiceTest {

    private final EmailVerificationOutboxService service = new EmailVerificationOutboxService(new ObjectMapper());

    @Test
    void build_includesVerificationCodeAndLegacyTokenAlias() {
        UUID userId = UUID.randomUUID();
        User user = User.rehydrate(
                userId,
                EmailAddress.of("user@example.com"),
                null,
                UserStatus.PENDING_VERIFICATION,
                false,
                false,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        OutboxEvent event = service.build(user, "123456", Instant.now());

        assertEquals("EMAIL_VERIFICATION_REQUESTED", event.eventType());
        assertTrue(event.payload().contains("\"verification_code\":\"123456\""));
        assertTrue(event.payload().contains("\"verification_token\":\"123456\""));
        assertTrue(event.payload().contains("\"verification_token_type\":\"" + VerificationTokenType.EMAIL_VERIFY.name() + "\""));
        assertTrue(event.payload().contains("\"email\":\"user@example.com\""));
        assertTrue(!event.payload().contains("verification_link"));
    }
}
