package com.twohands.auth_service.unit.application.auth.adminlogout;

import com.twohands.auth_service.application.auth.adminlogout.AdminLogoutCommand;
import com.twohands.auth_service.application.auth.adminlogout.AdminLogoutUseCase;
import com.twohands.auth_service.application.auth.logout.LogoutRateLimitService;
import com.twohands.auth_service.application.auth.logout.LogoutValidationService;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLogoutUseCaseTest {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);
    private final TokenHashingService tokenHashingService = Mockito.mock(TokenHashingService.class);
    private final LogoutRateLimitService logoutRateLimitService = Mockito.mock(LogoutRateLimitService.class);

    private AdminLogoutUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new AdminLogoutUseCase(
                new LogoutValidationService(),
                logoutRateLimitService,
                refreshTokenSessionRepository,
                tokenHashingService
        );
        when(tokenHashingService.sha256("raw-token")).thenReturn("hashed-token");
    }

    @Test
    void shouldRevokeOwnSession() {
        UUID userId = UUID.randomUUID();
        RefreshTokenSession session = activeSession(userId);
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(session));

        useCase.execute(new AdminLogoutCommand(userId, "raw-token", "127.0.0.1"));

        verify(refreshTokenSessionRepository).markLoggedOutIfActive(eq(session.id()), any(Instant.class));
    }

    @Test
    void shouldBeIdempotentWhenSessionNotFound() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        useCase.execute(new AdminLogoutCommand(userId, "raw-token", "127.0.0.1"));

        verify(refreshTokenSessionRepository, never()).markLoggedOutIfActive(any(), any());
    }

    @Test
    void shouldRejectLogoutForAnotherUsersRefreshToken() {
        UUID actorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        RefreshTokenSession session = activeSession(otherUserId);
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(session));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new AdminLogoutCommand(actorId, "raw-token", "127.0.0.1")
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(refreshTokenSessionRepository, never()).markLoggedOutIfActive(any(), any());
    }

    private RefreshTokenSession activeSession(UUID userId) {
        Instant now = Instant.now();
        return RefreshTokenSession.createActive(
                UUID.randomUUID(),
                userId,
                "hashed-token",
                "device",
                "127.0.0.1",
                "JUnit",
                now.plusSeconds(3600),
                now
        );
    }
}
