package com.twohands.auth_service.unit.application.admin.viewusersessionsforadmin;

import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminCommand;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminQueryValidationService;
import com.twohands.auth_service.application.admin.viewusersessionsforadmin.ViewUserSessionsForAdminUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionPage;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.domain.user.EmailAddress;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewUserSessionsForAdminUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = mock(RefreshTokenSessionRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = mock(PermissionQueryRepository.class);
    private final ViewUserSessionsForAdminQueryValidationService queryValidationService =
            new ViewUserSessionsForAdminQueryValidationService();

    private ViewUserSessionsForAdminUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewUserSessionsForAdminUseCase(
                userRepository,
                refreshTokenSessionRepository,
                permissionQueryRepository,
                queryValidationService
        );
    }

    @Test
    void shouldReturnActiveSessionsForAuthorizedActor() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        Instant now = Instant.now();
        RefreshTokenSession session = RefreshTokenSession.createActive(
                UUID.randomUUID(),
                targetUserId,
                "hash",
                "device-1",
                "203.0.113.1",
                "Chrome",
                now.plusSeconds(3600),
                now
        );

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_INVESTIGATION_READ"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));
        when(refreshTokenSessionRepository.findPageByUserId(
                eq(targetUserId),
                eq(SessionStatus.ACTIVE),
                eq(20),
                eq(0)
        )).thenReturn(new RefreshTokenSessionPage(List.of(session), 1));

        var result = useCase.execute(new ViewUserSessionsForAdminCommand(
                actorId,
                targetUserId,
                "ACTIVE",
                1,
                20
        ));

        assertThat(result.userId()).isEqualTo(targetUserId);
        assertThat(result.sessions()).hasSize(1);
        assertThat(result.sessions().getFirst().deviceId()).isEqualTo("device-1");
        assertThat(result.sessions().getFirst().sessionId()).isEqualTo(session.id());
        assertThat(result.pagination().totalItems()).isEqualTo(1);
        assertThat(result.pagination().hasNext()).isFalse();
    }

    @Test
    void shouldQueryAllStatusesWhenRequested() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_INVESTIGATION_READ"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));
        when(refreshTokenSessionRepository.findPageByUserId(
                eq(targetUserId),
                isNull(),
                eq(20),
                eq(0)
        )).thenReturn(new RefreshTokenSessionPage(List.of(), 0));

        useCase.execute(new ViewUserSessionsForAdminCommand(actorId, targetUserId, "ALL", 1, 20));

        verify(refreshTokenSessionRepository).findPageByUserId(eq(targetUserId), isNull(), eq(20), eq(0));
    }

    @Test
    void shouldRejectMissingPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_READ"));

        assertThatThrownBy(() -> useCase.execute(new ViewUserSessionsForAdminCommand(
                actorId,
                UUID.randomUUID(),
                "ACTIVE",
                1,
                20
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

        assertThatThrownBy(() -> useCase.execute(new ViewUserSessionsForAdminCommand(
                actorId,
                targetUserId,
                "ACTIVE",
                1,
                20
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
