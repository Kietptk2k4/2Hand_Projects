package com.twohands.auth_service.unit.application.auth.refresh;

import com.twohands.auth_service.application.auth.refresh.RefreshAccessTokenCommand;
import com.twohands.auth_service.application.auth.refresh.RefreshAccessTokenUseCase;
import com.twohands.auth_service.application.auth.refresh.RefreshAccessTokenValidationService;
import com.twohands.auth_service.application.auth.refresh.RefreshRateLimitService;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class RefreshAccessTokenUseCaseTest {

    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final TokenHashingService tokenHashingService = Mockito.mock(TokenHashingService.class);
    private final JwtTokenIssuer jwtTokenIssuer = Mockito.mock(JwtTokenIssuer.class);
    private final RefreshRateLimitService refreshRateLimitService = Mockito.mock(RefreshRateLimitService.class);

    private RefreshAccessTokenUseCase useCase;
    private UUID userId;

    @BeforeEach
    void setup() {
        useCase = new RefreshAccessTokenUseCase(
                new RefreshAccessTokenValidationService(),
                refreshTokenSessionRepository,
                userRepository,
                tokenHashingService,
                jwtTokenIssuer,
                refreshRateLimitService
        );
        userId = UUID.randomUUID();

        when(tokenHashingService.sha256(anyString())).thenReturn("hashed-token");
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser(userId)));
        when(jwtTokenIssuer.issueAccessToken(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new JwtTokenIssuer.AccessTokenOnly("access-token", Instant.now().plusSeconds(900), 900));
    }

    @Test
    void shouldRejectRevokedSession() {
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(session(SessionStatus.REVOKED, Instant.now().plusSeconds(600))));

        assertThrows(AppException.class, () -> useCase.execute(new RefreshAccessTokenCommand("refresh", "127.0.0.1")));
    }

    @Test
    void shouldRejectLoggedOutSession() {
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(session(SessionStatus.LOGGED_OUT, Instant.now().plusSeconds(600))));

        assertThrows(AppException.class, () -> useCase.execute(new RefreshAccessTokenCommand("refresh", "127.0.0.1")));
    }

    @Test
    void shouldRejectExpiredSessionByStatus() {
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(session(SessionStatus.EXPIRED, Instant.now().plusSeconds(600))));

        assertThrows(AppException.class, () -> useCase.execute(new RefreshAccessTokenCommand("refresh", "127.0.0.1")));
    }

    @Test
    void shouldRejectExpiredSessionByTime() {
        when(refreshTokenSessionRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(session(SessionStatus.ACTIVE, Instant.now().minusSeconds(1))));

        assertThrows(AppException.class, () -> useCase.execute(new RefreshAccessTokenCommand("refresh", "127.0.0.1")));
    }

    private RefreshTokenSession session(SessionStatus status, Instant expiresAt) {
        Instant now = Instant.now();
        return new RefreshTokenSession(
                UUID.randomUUID(),
                userId,
                "hashed-token",
                null,
                "127.0.0.1",
                "JUnit",
                expiresAt,
                status,
                now,
                now
        );
    }

    private User activeUser(UUID id) {
        Instant now = Instant.now();
        return User.rehydrate(
                id,
                EmailAddress.of("refresh-test@example.com"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                UserStatus.ACTIVE,
                true,
                false,
                null,
                null,
                null,
                now.minusSeconds(1000),
                now.minusSeconds(1000)
        );
    }
}
