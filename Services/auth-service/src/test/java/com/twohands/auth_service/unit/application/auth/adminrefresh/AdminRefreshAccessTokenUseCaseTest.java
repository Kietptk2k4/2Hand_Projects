package com.twohands.auth_service.unit.application.auth.adminrefresh;

import com.twohands.auth_service.application.auth.adminrefresh.AdminRefreshAccessTokenCommand;
import com.twohands.auth_service.application.auth.adminrefresh.AdminRefreshAccessTokenUseCase;
import com.twohands.auth_service.application.auth.refresh.RefreshAccessTokenValidationService;
import com.twohands.auth_service.application.auth.refresh.RefreshRateLimitService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenIssuer;
import com.twohands.auth_service.security.token.TokenHashingService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminRefreshAccessTokenUseCaseTest {

    private final RefreshAccessTokenValidationService validationService = new RefreshAccessTokenValidationService();
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = mock(RefreshTokenSessionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = mock(PermissionQueryRepository.class);
    private final TokenHashingService tokenHashingService = mock(TokenHashingService.class);
    private final JwtTokenIssuer jwtTokenIssuer = mock(JwtTokenIssuer.class);
    private final RefreshRateLimitService refreshRateLimitService = mock(RefreshRateLimitService.class);

    private final AdminRefreshAccessTokenUseCase useCase = new AdminRefreshAccessTokenUseCase(
            validationService,
            refreshTokenSessionRepository,
            userRepository,
            permissionQueryRepository,
            tokenHashingService,
            jwtTokenIssuer,
            refreshRateLimitService
    );

    @Test
    void shouldIssueAdminAccessTokenForValidSession() {
        UUID userId = UUID.randomUUID();
        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                userId,
                "hash",
                "device",
                "127.0.0.1",
                "JUnit",
                Instant.now().plusSeconds(3600),
                Instant.now()
        );
        Instant now = Instant.now();
        User user = User.rehydrate(
                userId,
                EmailAddress.of("admin@2hands.vn"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                UserStatus.ACTIVE,
                true,
                false,
                null,
                null,
                null,
                now,
                now
        );

        when(tokenHashingService.sha256("refresh-raw")).thenReturn("hash");
        when(refreshTokenSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(permissionQueryRepository.findRoleCodesByUserId(userId)).thenReturn(List.of("ADMIN"));
        when(permissionQueryRepository.findPermissionCodesByUserId(userId)).thenReturn(Set.of("ADMIN_ACCESS"));
        when(jwtTokenIssuer.issueAdminAccessOnly(
                eq(userId),
                eq("admin@2hands.vn"),
                eq("ACTIVE"),
                any(),
                any(),
                any(Instant.class)
        )).thenReturn(new JwtTokenIssuer.AccessTokenOnly("jwt-access", Instant.now().plusSeconds(900), 900));

        var result = useCase.execute(new AdminRefreshAccessTokenCommand("refresh-raw", "127.0.0.1"));

        assertEquals("jwt-access", result.accessToken());
        assertEquals(900, result.expiresIn());
        verify(refreshRateLimitService).validateRefreshAttempt("127.0.0.1");
    }

    @Test
    void shouldRejectWhenUserNoLongerHasAdminAccess() {
        UUID userId = UUID.randomUUID();
        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                userId,
                "hash",
                null,
                null,
                null,
                Instant.now().plusSeconds(3600),
                Instant.now()
        );
        Instant now = Instant.now();
        User user = User.rehydrate(
                userId,
                EmailAddress.of("user@2hands.vn"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                UserStatus.ACTIVE,
                true,
                false,
                null,
                null,
                null,
                now,
                now
        );

        when(tokenHashingService.sha256("refresh-raw")).thenReturn("hash");
        when(refreshTokenSessionRepository.findByTokenHash("hash")).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(permissionQueryRepository.findRoleCodesByUserId(userId)).thenReturn(List.of());
        when(permissionQueryRepository.findPermissionCodesByUserId(userId)).thenReturn(Set.of());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new AdminRefreshAccessTokenCommand("refresh-raw", "127.0.0.1")
        ));
        assertEquals(ErrorCode.ADMIN_PORTAL_ACCESS_DENIED, ex.getErrorCode());
    }
}
