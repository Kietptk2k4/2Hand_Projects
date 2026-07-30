package com.twohands.auth_service.unit.application.session;

import com.twohands.auth_service.application.session.RevokeAllUserSessionsService;
import com.twohands.auth_service.domain.session.AccessTokenInvalidationStore;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeAllUserSessionsServiceTest {

    @Mock
    private RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Mock
    private AccessTokenInvalidationStore accessTokenInvalidationStore;

    @InjectMocks
    private RevokeAllUserSessionsService service;

    @Test
    void shouldRevokeRefreshSessionsAndInvalidateAccessTokens() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenSessionRepository.revokeAllByUserId(userId)).thenReturn(2);

        int revokedCount = service.revokeAll(userId);

        assertThat(revokedCount).isEqualTo(2);
        verify(refreshTokenSessionRepository).revokeAllByUserId(userId);
        verify(accessTokenInvalidationStore).invalidateTokensIssuedBefore(eq(userId), any());
    }
}
