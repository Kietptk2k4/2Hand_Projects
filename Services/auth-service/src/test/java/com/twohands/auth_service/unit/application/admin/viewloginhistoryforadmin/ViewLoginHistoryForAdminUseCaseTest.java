package com.twohands.auth_service.unit.application.admin.viewloginhistoryforadmin;

import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminCommand;
import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminQueryValidationService;
import com.twohands.auth_service.application.admin.viewloginhistoryforadmin.ViewLoginHistoryForAdminUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogPage;
import com.twohands.auth_service.domain.user.LoginLogQueryFilter;
import com.twohands.auth_service.domain.user.LoginLogRepository;
import com.twohands.auth_service.domain.user.LoginMethod;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViewLoginHistoryForAdminUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final LoginLogRepository loginLogRepository = mock(LoginLogRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = mock(PermissionQueryRepository.class);
    private final ViewLoginHistoryForAdminQueryValidationService queryValidationService =
            new ViewLoginHistoryForAdminQueryValidationService();

    private ViewLoginHistoryForAdminUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewLoginHistoryForAdminUseCase(
                userRepository,
                loginLogRepository,
                permissionQueryRepository,
                queryValidationService
        );
    }

    @Test
    void shouldReturnLoginHistoryForAuthorizedActor() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Instant now = Instant.now();
        LoginLog log = new LoginLog(
                UUID.randomUUID(),
                targetUserId,
                LoginMethod.EMAIL,
                "203.0.113.1",
                "Chrome",
                true,
                now
        );

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_INVESTIGATION_READ"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));
        when(loginLogRepository.findPageByUserId(
                eq(targetUserId),
                any(LoginLogQueryFilter.class),
                eq(20),
                eq(0)
        )).thenReturn(new LoginLogPage(List.of(log), 1));

        var result = useCase.execute(new ViewLoginHistoryForAdminCommand(
                actorId,
                targetUserId,
                1,
                20,
                null,
                null,
                null
        ));

        assertThat(result.userId()).isEqualTo(targetUserId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().loginMethod()).isEqualTo("EMAIL");
        assertThat(result.pagination().totalItems()).isEqualTo(1);
        assertThat(result.pagination().totalPages()).isEqualTo(1);
        assertThat(result.pagination().hasNext()).isFalse();
    }

    @Test
    void shouldRejectMissingPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_READ"));

        assertThatThrownBy(() -> useCase.execute(new ViewLoginHistoryForAdminCommand(
                actorId,
                UUID.randomUUID(),
                1,
                20,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void shouldReturn404WhenTargetUserMissing() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_INVESTIGATION_READ"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewLoginHistoryForAdminCommand(
                actorId,
                targetUserId,
                1,
                20,
                null,
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    private User activeUser(UUID userId) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("user@example.com"),
                PasswordHash.of("hash"),
                UserStatus.ACTIVE,
                true,
                false,
                null,
                null,
                null,
                now,
                now
        );
    }
}
