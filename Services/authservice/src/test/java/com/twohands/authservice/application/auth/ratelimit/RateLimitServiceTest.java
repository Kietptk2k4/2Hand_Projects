package com.twohands.authservice.application.auth.ratelimit;

import com.twohands.authservice.application.auth.port.RateLimitStore;
import com.twohands.authservice.delivery.http.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RateLimitStore rateLimitStore;

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(rateLimitStore);
        ReflectionTestUtils.setField(rateLimitService, "max", 5);
        ReflectionTestUtils.setField(rateLimitService, "window", 60L);
    }

    @Test
    @DisplayName("should allow request when under limit")
    void shouldAllowWhenUnderLimit() {
        when(rateLimitStore.increment("auth.rate-limit:verify:127.0.0.1", 60L)).thenReturn(3L);

        assertThatCode(() -> rateLimitService.check("verify", "127.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should allow request exactly at the limit")
    void shouldAllowAtExactLimit() {
        when(rateLimitStore.increment("auth.rate-limit:login:10.0.0.1", 60L)).thenReturn(5L);

        assertThatCode(() -> rateLimitService.check("login", "10.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should throw TooManyRequestsException when limit exceeded")
    void shouldThrowWhenLimitExceeded() {
        when(rateLimitStore.increment("auth.rate-limit:verify:127.0.0.1", 60L)).thenReturn(6L);

        assertThatThrownBy(() -> rateLimitService.check("verify", "127.0.0.1"))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("Too many requests");
    }

    @Test
    @DisplayName("should use separate rate limit keys per action and IP")
    void shouldUseIsolatedKeys() {
        when(rateLimitStore.increment("auth.rate-limit:verify:1.1.1.1", 60L)).thenReturn(10L);
        when(rateLimitStore.increment("auth.rate-limit:verify:2.2.2.2", 60L)).thenReturn(1L);

        assertThatThrownBy(() -> rateLimitService.check("verify", "1.1.1.1"))
                .isInstanceOf(TooManyRequestsException.class);

        assertThatCode(() -> rateLimitService.check("verify", "2.2.2.2"))
                .doesNotThrowAnyException();
    }
}
