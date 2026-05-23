package com.twohands.auth_service.unit.application.admin.restrictuser;

import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminCommand;
import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.user.EmailAddress;
import com.twohands.auth_service.domain.user.PasswordHash;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestrictUserByAdminUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private RestrictUserByAdminUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RestrictUserByAdminUseCase(
                userRepository,
                permissionQueryRepository
        );
    }

    @Test
    void shouldValidateUserWithoutChangingStatusOrRevokingSessions() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_RESTRICT"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(activeUser(targetUserId)));

        var result = useCase.execute(new RestrictUserByAdminCommand(
                actorId,
                targetUserId,
                UUID.randomUUID(),
                "SPAM",
                "Spam comments",
                null
        ));

        assertEquals(targetUserId, result.userId());
        assertEquals("ACTIVE", result.status());
        assertEquals(0, result.revokedSessionCount());
        verify(userRepository, never()).updateStatus(any(), any(), any());
        verify(refreshTokenSessionRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void shouldReturn404WhenUserMissing() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_RESTRICT"));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new RestrictUserByAdminCommand(actorId, UUID.randomUUID(), UUID.randomUUID(), "SPAM", "Spam", null)
        ));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldRejectWhenActorLacksPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of());
        when(permissionQueryRepository.findRoleCodesByUserId(actorId)).thenReturn(List.of("MODERATOR"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new RestrictUserByAdminCommand(actorId, UUID.randomUUID(), UUID.randomUUID(), "SPAM", "Spam", null)
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private User activeUser(UUID userId) {
        Instant now = Instant.now();
        return User.rehydrate(
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
    }
}
