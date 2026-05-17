package com.twohands.auth_service.unit.application.rbac.viewrolelist;

import com.twohands.auth_service.application.rbac.viewrolelist.ViewRoleListResult;
import com.twohands.auth_service.application.rbac.viewrolelist.ViewRoleListUseCase;
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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewRoleListUseCaseTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private ViewRoleListUseCase useCase;
    private UUID actorUserId;

    @BeforeEach
    void setup() {
        useCase = new ViewRoleListUseCase(roleRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
    }

    @Test
    void shouldReturnRoleListSuccessfully() {
        Role role1 = buildRole("ADMIN", "Administrator");
        Role role2 = buildRole("MODERATOR", "Moderator");
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

        ViewRoleListResult result = useCase.execute(actorUserId);

        assertEquals(2, result.roles().size());
        assertEquals("ADMIN", result.roles().get(0).code());
        assertEquals("MODERATOR", result.roles().get(1).code());
        verify(roleRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoRoles() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findAll()).thenReturn(List.of());

        ViewRoleListResult result = useCase.execute(actorUserId);

        assertEquals(0, result.roles().size());
    }

    @Test
    void shouldReturnForbiddenWhenActorHasNoPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("USER_READ"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(actorUserId));

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    private Role buildRole(String code, String name) {
        Instant now = Instant.now();
        return new Role(UUID.randomUUID(), code, name, Set.of(), now.minusSeconds(100), now.minusSeconds(100));
    }
}
