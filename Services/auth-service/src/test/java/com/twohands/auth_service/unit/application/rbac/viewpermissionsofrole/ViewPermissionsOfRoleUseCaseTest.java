package com.twohands.auth_service.unit.application.rbac.viewpermissionsofrole;

import com.twohands.auth_service.application.rbac.viewpermissionsofrole.ViewPermissionsOfRoleResult;
import com.twohands.auth_service.application.rbac.viewpermissionsofrole.ViewPermissionsOfRoleUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
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
import static org.mockito.Mockito.when;

class ViewPermissionsOfRoleUseCaseTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private ViewPermissionsOfRoleUseCase useCase;
    private UUID actorUserId;
    private UUID roleId;

    @BeforeEach
    void setup() {
        useCase = new ViewPermissionsOfRoleUseCase(roleRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    void shouldReturnPermissionsSuccessfully() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(buildRole(roleId, "ADMIN", "Administrator")));
        when(permissionQueryRepository.findPermissionsByRoleId(roleId)).thenReturn(List.of(
                new PermissionQueryRepository.PermissionData("USER_READ", "Read user information"),
                new PermissionQueryRepository.PermissionData("USER_UPDATE", "Update user information")
        ));

        ViewPermissionsOfRoleResult result = useCase.execute(actorUserId, roleId);

        assertEquals("ADMIN", result.role().code());
        assertEquals(2, result.permissions().size());
        assertEquals("USER_READ", result.permissions().get(0).code());
    }

    @Test
    void shouldReturnEmptyPermissionsWhenRoleHasNoPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(buildRole(roleId, "MODERATOR", "Moderator")));
        when(permissionQueryRepository.findPermissionsByRoleId(roleId)).thenReturn(List.of());

        ViewPermissionsOfRoleResult result = useCase.execute(actorUserId, roleId);

        assertEquals("MODERATOR", result.role().code());
        assertEquals(0, result.permissions().size());
    }

    @Test
    void shouldReturnNotFoundWhenRoleMissing() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(actorUserId, roleId));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void shouldReturnForbiddenWhenActorMissingPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("USER_READ"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(actorUserId, roleId));

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private Role buildRole(UUID id, String code, String name) {
        Instant now = Instant.now();
        return new Role(id, code, name, Set.of(), now.minusSeconds(100), now.minusSeconds(100));
    }
}
