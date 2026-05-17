package com.twohands.auth_service.unit.application.useraccount.trackloginhistory;

import com.twohands.auth_service.application.useraccount.common.UserAccountAuthContextService;
import com.twohands.auth_service.application.useraccount.trackloginhistory.TrackLoginHistoryResult;
import com.twohands.auth_service.application.useraccount.trackloginhistory.TrackLoginHistoryUseCase;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.LoginMethod;
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

class TrackLoginHistoryUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final LoginLogRepository loginLogRepository = Mockito.mock(LoginLogRepository.class);

    private TrackLoginHistoryUseCase useCase;
    private UUID userId;

    @BeforeEach
    void setup() {
        useCase = new TrackLoginHistoryUseCase(
                userRepository,
                loginLogRepository,
                new UserAccountAuthContextService()
        );
        userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId)));
    }

    @Test
    void shouldReturnItemsWithLimitOffset() {
        Instant now = Instant.now();
        LoginLog first = new LoginLog(UUID.randomUUID(), userId, LoginMethod.EMAIL, "127.0.0.1", "JUnit", true, now);
        LoginLog second = new LoginLog(UUID.randomUUID(), userId, LoginMethod.GOOGLE, "10.0.0.1", "Chrome", false, now.minusSeconds(60));
        when(loginLogRepository.findByUserId(userId, 20, 0)).thenReturn(List.of(first, second));

        TrackLoginHistoryResult result = useCase.execute(userId, 20, 0);

        assertEquals(2, result.items().size());
        assertEquals(20, result.limit());
        assertEquals(0, result.offset());
        assertEquals("EMAIL", result.items().get(0).loginMethod());
        assertEquals(true, result.items().get(0).success());
        assertEquals(false, result.items().get(1).success());
        verify(loginLogRepository).findByUserId(userId, 20, 0);
    }

    @Test
    void shouldReturnEmptyItemsWhenNoLogs() {
        when(loginLogRepository.findByUserId(userId, 10, 5)).thenReturn(List.of());

        TrackLoginHistoryResult result = useCase.execute(userId, 10, 5);

        assertEquals(0, result.items().size());
        assertEquals(10, result.limit());
        assertEquals(5, result.offset());
    }

    private User buildUser(UUID id) {
        Instant now = Instant.now();
        return User.rehydrate(
                id,
                EmailAddress.of("track-history@example.com"),
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
