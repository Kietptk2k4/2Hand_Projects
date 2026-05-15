package com.twohands.auth_service.unit.application.auth.logout;

import com.twohands.auth_service.application.auth.logout.LogoutCommand;
import com.twohands.auth_service.application.auth.logout.LogoutRateLimitService;
import com.twohands.auth_service.application.auth.logout.LogoutUseCase;
import com.twohands.auth_service.application.auth.logout.LogoutValidationService;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogoutUseCaseTest {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);
    private final TokenHashingService tokenHashingService = Mockito.mock(TokenHashingService.class);
    private final LogoutRateLimitService logoutRateLimitService = Mockito.mock(LogoutRateLimitService.class);

    private LogoutUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new LogoutUseCase(
                new LogoutValidationService(),
                logoutRateLimitService,
                refreshTokenSessionRepository,
                tokenHashingService
        );
        when(tokenHashingService.sha256("raw-token")).thenReturn("hashed-token");
    }

    @Test
    void shouldBeIdempotentWhenSessionNotFound() {
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        useCase.execute(new LogoutCommand("raw-token", "127.0.0.1"));

        verify(refreshTokenSessionRepository, never()).markLoggedOutIfActive(any(), any());
    }

    @Test
    void shouldBeIdempotentWhenSessionAlreadyLoggedOut() {
        RefreshTokenSession alreadyLoggedOut = session(SessionStatus.LOGGED_OUT);
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(alreadyLoggedOut));

        useCase.execute(new LogoutCommand("raw-token", "127.0.0.1"));

        verify(refreshTokenSessionRepository).markLoggedOutIfActive(Mockito.eq(alreadyLoggedOut.id()), any(Instant.class));
    }

    private RefreshTokenSession session(SessionStatus status) {
        Instant now = Instant.now();
        return new RefreshTokenSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hashed-token",
                null,
                "127.0.0.1",
                "JUnit",
                now.plusSeconds(300),
                status,
                now,
                now
        );
    }
}
