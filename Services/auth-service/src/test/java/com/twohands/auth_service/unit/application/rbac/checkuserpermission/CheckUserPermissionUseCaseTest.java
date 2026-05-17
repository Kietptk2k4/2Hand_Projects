package com.twohands.auth_service.unit.application.rbac.checkuserpermission;

import com.twohands.auth_service.application.rbac.checkuserpermission.CheckUserPermissionResult;
import com.twohands.auth_service.application.rbac.checkuserpermission.CheckUserPermissionUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CheckUserPermissionUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private CheckUserPermissionUseCase useCase;
    private UUID actorUserId;
    private UUID targetUserId;

    @BeforeEach
    void setup() {
        useCase = new CheckUserPermissionUseCase(userRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
    }

    @Test
    void shouldReturnPermissionsSuccessfully() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(targetUserId, UserStatus.ACTIVE)));
        when(permissionQueryRepository.findPermissionCodesByUserId(targetUserId))
                .thenReturn(Set.of("USER_UPDATE", "ASSIGN_ROLE"));

        CheckUserPermissionResult result = useCase.execute(actorUserId, targetUserId);

        assertEquals(targetUserId, result.userId());
        assertEquals(2, result.permissions().size());
        assertEquals("ASSIGN_ROLE", result.permissions().get(0).code());
        assertEquals("USER_UPDATE", result.permissions().get(1).code());
    }

    @Test
    void shouldReturnEmptyPermissionsWhenTargetUserHasNoPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(targetUserId, UserStatus.ACTIVE)));
        when(permissionQueryRepository.findPermissionCodesByUserId(targetUserId)).thenReturn(Set.of());

        CheckUserPermissionResult result = useCase.execute(actorUserId, targetUserId);

        assertEquals(targetUserId, result.userId());
        assertEquals(0, result.permissions().size());
    }

    @Test
    void shouldReturnForbiddenWhenActorMissingPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("USER_READ"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(actorUserId, targetUserId));

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    @Test
    void shouldReturnNotFoundWhenTargetUserMissing() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(actorUserId, targetUserId));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldReturnNotFoundWhenTargetUserDeleted() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(targetUserId, UserStatus.DELETED)));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(actorUserId, targetUserId));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    private User buildUser(UUID userId, UserStatus status) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("check-user-permission@example.com"),
                PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv"),
                status,
                true,
                false,
                null,
                null,
                status == UserStatus.DELETED ? now : null,
                now.minusSeconds(100),
                now.minusSeconds(100)
        );
    }
}
