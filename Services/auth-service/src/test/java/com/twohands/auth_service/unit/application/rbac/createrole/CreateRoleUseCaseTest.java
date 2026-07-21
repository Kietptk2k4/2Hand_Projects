package com.twohands.auth_service.unit.application.rbac.createrole;

import com.twohands.auth_service.application.rbac.createrole.CreateRoleCommand;
import com.twohands.auth_service.application.rbac.createrole.CreateRoleResult;
import com.twohands.auth_service.application.rbac.createrole.CreateRoleUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

class CreateRoleUseCaseTest {

    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private CreateRoleUseCase useCase;
    private UUID actorUserId;

    @BeforeEach
    void setup() {
        useCase = new CreateRoleUseCase(roleRepository, permissionQueryRepository);
        actorUserId = UUID.randomUUID();
    }

    @Test
    void shouldCreateRoleSuccessfully() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findByCode("SUPPORT")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateRoleResult result = useCase.execute(new CreateRoleCommand(actorUserId, "support", "Support team"));

        assertEquals("SUPPORT", result.code());
        assertEquals("Support team", result.name());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldReturnConflictWhenCodeAlreadyExists() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findByCode("SUPPORT"))
                .thenReturn(Optional.of(new Role(UUID.randomUUID(), "SUPPORT", "Support", Set.of(), Instant.now(), Instant.now())));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new CreateRoleCommand(actorUserId, "SUPPORT", "Support team"))
        );

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals("code", ex.getField());
        assertEquals("DUPLICATE", ex.getReason());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldReturnConflictWhenCodeIsReservedSystemRole() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new CreateRoleCommand(actorUserId, "ADMIN", "Duplicate admin"))
        );

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals("code", ex.getField());
        assertEquals("RESERVED", ex.getReason());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldReturnForbiddenWhenActorMissingPermission() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("USER_READ"));

        AppException ex = assertThrows(
                AppException.class,
                () -> useCase.execute(new CreateRoleCommand(actorUserId, "SUPPORT", "Support team"))
        );

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldNormalizeCodeToUppercase() {
        when(permissionQueryRepository.findPermissionCodesByUserId(actorUserId)).thenReturn(Set.of("ASSIGN_ROLE"));
        when(roleRepository.findByCode("CUSTOM_ROLE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new CreateRoleCommand(actorUserId, " custom_role ", "Custom role"));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals("CUSTOM_ROLE", captor.getValue().code());
    }
}
