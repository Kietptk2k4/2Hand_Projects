package com.twohands.auth_service.unit.application.useraccount.viewloginsesssionlist;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.application.useraccount.viewloginsesssionlist.ViewLoginSesssionListResult;
import com.twohands.auth_service.application.useraccount.viewloginsesssionlist.ViewLoginSesssionListUseCase;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewLoginSesssionListUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);

    private ViewLoginSesssionListUseCase useCase;
    private UUID userId;

    @BeforeEach
    void setup() {
        useCase = new ViewLoginSesssionListUseCase(
                userRepository,
                refreshTokenSessionRepository,
                new UserAccountAuthContextService()
        );
        userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId)));
    }

    @Test
    void shouldReturnActiveSessions() {
        RefreshTokenSession first = buildSession("device-1", "127.0.0.1", Instant.now().minusSeconds(120));
        RefreshTokenSession second = buildSession("device-2", "127.0.0.2", Instant.now().minusSeconds(60));
        when(refreshTokenSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE))
                .thenReturn(List.of(second, first));

        ViewLoginSesssionListResult result = useCase.execute(userId);

        assertEquals(2, result.sessions().size());
        assertEquals("device-2", result.sessions().get(0).deviceId());
        assertEquals("ACTIVE", result.sessions().get(0).status());
        assertEquals("127.0.0.1", result.sessions().get(1).ipAddress());
        verify(refreshTokenSessionRepository).findByUserIdAndStatus(userId, SessionStatus.ACTIVE);
    }

    @Test
    void shouldReturnEmptyWhenNoActiveSession() {
        when(refreshTokenSessionRepository.findByUserIdAndStatus(userId, SessionStatus.ACTIVE))
                .thenReturn(List.of());

        ViewLoginSesssionListResult result = useCase.execute(userId);

        assertEquals(0, result.sessions().size());
    }

    private User buildUser(UUID id) {
        Instant now = Instant.now();
        return User.rehydrate(
                id,
                EmailAddress.of("view-session@example.com"),
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

    private RefreshTokenSession buildSession(String deviceId, String ipAddress, Instant createdAt) {
        return new RefreshTokenSession(
                UUID.randomUUID(),
                userId,
                "hash-" + UUID.randomUUID(),
                deviceId,
                ipAddress,
                "JUnit",
                createdAt.plusSeconds(3600),
                SessionStatus.ACTIVE,
                createdAt,
                createdAt
        );
    }
}
