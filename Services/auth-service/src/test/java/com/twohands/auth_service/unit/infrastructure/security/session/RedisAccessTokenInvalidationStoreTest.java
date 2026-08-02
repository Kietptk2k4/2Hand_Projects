package com.twohands.auth_service.unit.infrastructure.security.session;

import com.twohands.auth_service.infrastructure.security.session.RedisAccessTokenInvalidationStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAccessTokenInvalidationStoreTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisAccessTokenInvalidationStore store;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisAccessTokenInvalidationStore(stringRedisTemplate, 900_000L);
    }

    private static String redisKey(UUID userId) {
        return RedisAccessTokenInvalidationStore.KEY_PREFIX + userId;
    }

    @Test
    void shouldMarkTokenIssuedBeforeInvalidationAsRevoked() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get(redisKey(userId))).thenReturn("1700000000000");

        assertThat(store.isTokenInvalidated(userId, 1699999999999L)).isTrue();
        assertThat(store.isTokenInvalidated(userId, 1700000000000L)).isFalse();
    }

    @Test
    void shouldStoreInvalidBeforeTimestampWithTtl() {
        UUID userId = UUID.randomUUID();
        Instant invalidBefore = Instant.ofEpochMilli(1_700_000_000_123L);
        when(valueOperations.get(redisKey(userId))).thenReturn(null);

        store.invalidateTokensIssuedBefore(userId, invalidBefore);

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).set(
                eq(redisKey(userId)),
                eq("1700000000123"),
                ttlCaptor.capture()
        );
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMillis(900_000L).plusMinutes(1));
    }

    @Test
    void shouldKeepLatestInvalidBeforeTimestamp() {
        UUID userId = UUID.randomUUID();
        when(valueOperations.get(redisKey(userId))).thenReturn("2000");

        store.invalidateTokensIssuedBefore(userId, Instant.ofEpochMilli(1000L));

        verify(valueOperations, org.mockito.Mockito.never()).set(any(), any(), any(Duration.class));
    }
}
