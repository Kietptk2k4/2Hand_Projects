package com.twohands.auth_service.unit.application.rbac.updaterole;

import com.twohands.auth_service.application.rbac.updaterole.UpdateRoleCommand;
import com.twohands.auth_service.application.rbac.updaterole.UpdateRoleResult;
import com.twohands.auth_service.application.rbac.updaterole.UpdateRoleUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
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

class UpdateRoleUseCaseTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private UpdateRoleUseCase useCase;
    private UUID actorUserId;
    private UUID roleId;

    @BeforeEach
    void setup() {
        useCase = new UpdateRoleUseCase(roleRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    void shouldUpdateRoleNameSuccessfully() {
        Role role = buildRole(roleId, "SUPPORT", "Support");
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateRoleResult result = useCase.execute(new UpdateRoleCommand(actorUserId, roleId, "Support team"));

        assertEquals("Support team", result.name());
        assertEquals("SUPPORT", result.code());
        verify(roleRepository).save(role);
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingSystemRole() {
        Role role = buildRole(roleId, "ADMIN", "Administrator");
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new UpdateRoleCommand(actorUserId, roleId, "Super admin"))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        assertEquals("code", ex.getField());
        assertEquals("SYSTEM_ROLE_PROTECTED", ex.getReason());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldReturnNotFoundWhenRoleMissing() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new UpdateRoleCommand(actorUserId, roleId, "Support team"))
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    private Role buildRole(UUID id, String code, String name) {
        Instant now = Instant.now();
        return new Role(id, code, name, Set.of(), now, now);
    }
}
