package com.twohands.auth_service.unit.application.useraccount.logoutallsesssion;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.application.useraccount.logoutallsesssion.LogoutAllSesssionUseCase;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogoutAllSesssionUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);

    private LogoutAllSesssionUseCase useCase;
    private UUID userId;

    @BeforeEach
    void setup() {
        useCase = new LogoutAllSesssionUseCase(
                userRepository,
                refreshTokenSessionRepository,
                new UserAccountAuthContextService()
        );
        userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId)));
    }

    @Test
    void shouldRevokeAllActiveSessions() {
        useCase.execute(userId);

        verify(refreshTokenSessionRepository).revokeAllByUserId(userId);
    }

    @Test
    void shouldStillSucceedWhenNoActiveSessions() {
        useCase.execute(userId);

        verify(refreshTokenSessionRepository).revokeAllByUserId(userId);
    }

    private User buildUser(UUID id) {
        Instant now = Instant.now();
        return User.rehydrate(
                id,
                EmailAddress.of("logout-all-user@example.com"),
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
