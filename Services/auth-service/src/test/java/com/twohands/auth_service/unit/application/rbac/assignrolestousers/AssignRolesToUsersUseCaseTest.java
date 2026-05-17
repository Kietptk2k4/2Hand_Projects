package com.twohands.auth_service.unit.application.rbac.assignrolestousers;

import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersCommand;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersResult;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.domain.rbac.UserRoleAssignment;
import com.twohands.auth_service.domain.rbac.UserRoleAssignmentRepository;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignRolesToUsersUseCaseTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final UserRoleAssignmentRepository userRoleAssignmentRepository = Mockito.mock(UserRoleAssignmentRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);
    private final RefreshTokenSessionRepository refreshTokenSessionRepository = Mockito.mock(RefreshTokenSessionRepository.class);

    private AssignRolesToUsersUseCase useCase;

    private UUID actorUserId;
    private UUID targetUserId;
    private UUID roleId;

    @BeforeEach
    void setup() {
        useCase = new AssignRolesToUsersUseCase(
                userRepository,
                roleRepository,
                userRoleAssignmentRepository,
                permissionQueryRepository,
                refreshTokenSessionRepository
        );

        actorUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    void shouldAssignRoleSuccessfully() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(targetUserId, UserStatus.ACTIVE)));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(buildRole(roleId)));
        when(userRoleAssignmentRepository.findByUserId(targetUserId)).thenReturn(Optional.empty());

        AssignRolesToUsersResult result = useCase.execute(new AssignRolesToUsersCommand(actorUserId, targetUserId, roleId));

        assertEquals(targetUserId, result.userId());
        assertEquals(roleId, result.roleId());
        verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
        verify(refreshTokenSessionRepository).revokeAllByUserId(targetUserId);
    }

    @Test
    void shouldReturnConflictWhenRoleAlreadyAssigned() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(targetUserId, UserStatus.ACTIVE)));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(buildRole(roleId)));
        when(userRoleAssignmentRepository.findByUserId(targetUserId))
                .thenReturn(Optional.of(new UserRoleAssignment(targetUserId, Set.of(roleId), Instant.now(), Instant.now())));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new AssignRolesToUsersCommand(actorUserId, targetUserId, roleId))
        );

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals("role_id", ex.getField());
        assertEquals("ALREADY_ASSIGNED", ex.getReason());
        verify(userRoleAssignmentRepository, never()).save(any(UserRoleAssignment.class));
    }

    @Test
    void shouldReturnForbiddenWhenActorMissingPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("USER_READ"));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new AssignRolesToUsersCommand(actorUserId, targetUserId, roleId))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void shouldReturnNotFoundWhenTargetUserMissing() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new AssignRolesToUsersCommand(actorUserId, targetUserId, roleId))
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldReturnNotFoundWhenRoleMissing() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(buildUser(targetUserId, UserStatus.ACTIVE)));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new AssignRolesToUsersCommand(actorUserId, targetUserId, roleId))
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldReturnForbiddenWhenSelfAssign() {
        UUID sameUserId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(sameUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(userRepository.findById(sameUserId)).thenReturn(Optional.of(buildUser(sameUserId, UserStatus.ACTIVE)));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(buildRole(roleId)));
        when(userRoleAssignmentRepository.findByUserId(sameUserId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new AssignRolesToUsersCommand(sameUserId, sameUserId, roleId))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(userRoleAssignmentRepository, never()).save(any(UserRoleAssignment.class));
    }

    private User buildUser(UUID userId, UserStatus status) {
        Instant now = Instant.now();
        return User.rehydrate(
                userId,
                EmailAddress.of("assign-role@example.com"),
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

    private Role buildRole(UUID id) {
        Instant now = Instant.now();
        return new Role(id, "ADMIN", "Administrator", Set.of(), now.minusSeconds(100), now.minusSeconds(100));
    }
}
