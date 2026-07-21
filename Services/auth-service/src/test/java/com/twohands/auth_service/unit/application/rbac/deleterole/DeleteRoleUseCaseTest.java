package com.twohands.auth_service.unit.application.rbac.deleterole;

import com.twohands.auth_service.application.rbac.deleterole.DeleteRoleCommand;
import com.twohands.auth_service.application.rbac.deleterole.DeleteRoleResult;
import com.twohands.auth_service.application.rbac.deleterole.DeleteRoleUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.domain.rbac.UserRoleAssignmentRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteRoleUseCaseTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final UserRoleAssignmentRepository userRoleAssignmentRepository = Mockito.mock(UserRoleAssignmentRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private DeleteRoleUseCase useCase;
    private UUID actorUserId;
    private UUID roleId;

    @BeforeEach
    void setup() {
        useCase = new DeleteRoleUseCase(roleRepository, userRoleAssignmentRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        Role role = buildRole(roleId, "SUPPORT", "Support");
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleAssignmentRepository.countUsersByRoleId(roleId)).thenReturn(0L);

        DeleteRoleResult result = useCase.execute(new DeleteRoleCommand(actorUserId, roleId));

        assertEquals(roleId, result.id());
        assertEquals("SUPPORT", result.code());
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    void shouldReturnConflictWhenRoleStillAssignedToUsers() {
        Role role = buildRole(roleId, "SUPPORT", "Support");
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(userRoleAssignmentRepository.countUsersByRoleId(roleId)).thenReturn(2L);

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new DeleteRoleCommand(actorUserId, roleId))
        );

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals("role_id", ex.getField());
        assertEquals("IN_USE", ex.getReason());
        verify(roleRepository, never()).deleteById(roleId);
    }

    @Test
    void shouldReturnForbiddenWhenDeletingSystemRole() {
        Role role = buildRole(roleId, "MODERATOR", "Moderator");
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new DeleteRoleCommand(actorUserId, roleId))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        assertEquals("code", ex.getField());
        assertEquals("SYSTEM_ROLE_PROTECTED", ex.getReason());
        verify(roleRepository, never()).deleteById(roleId);
    }

    private Role buildRole(UUID id, String code, String name) {
        Instant now = Instant.now();
        return new Role(id, code, name, Set.of(), now, now);
    }
}
