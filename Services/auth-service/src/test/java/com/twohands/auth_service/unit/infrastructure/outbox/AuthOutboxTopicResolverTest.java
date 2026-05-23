package com.twohands.auth_service.unit.infrastructure.outbox;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.infrastructure.outbox.AuthOutboxTopicResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthOutboxTopicResolverTest {

    private final AuthOutboxTopicResolver resolver = new AuthOutboxTopicResolver();

    @Test
    void shouldResolveKnownEventTypes() {
        assertThat(resolver.resolve("USER_CREATED")).isEqualTo("auth.user.created");
        assertThat(resolver.resolve("EMAIL_VERIFICATION_REQUESTED")).isEqualTo("auth.email.verification_requested");
        assertThat(resolver.resolve("PASSWORD_RESET_REQUESTED")).isEqualTo("auth.password.reset_requested");
    }

    @Test
    void shouldThrowForUnknownEventType() {
        assertThatThrownBy(() -> resolver.resolve("UNKNOWN_EVENT"))
                .isInstanceOf(AppException.class);
    }
}
