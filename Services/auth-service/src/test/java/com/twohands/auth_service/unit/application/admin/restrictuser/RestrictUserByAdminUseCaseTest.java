package com.twohands.auth_service.unit.application.admin.restrictuser;

import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementResult;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementUseCase;
import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminCommand;
import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminUseCase;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RestrictUserByAdminUseCaseTest {

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase = Mockito.mock(ApplyUserEnforcementUseCase.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private RestrictUserByAdminUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RestrictUserByAdminUseCase(applyUserEnforcementUseCase, permissionQueryRepository);
    }

    @Test
    void shouldDelegateRestrictToApplyUseCase() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_RESTRICT"));
        when(applyUserEnforcementUseCase.execute(any(ApplyUserEnforcementCommand.class)))
                .thenReturn(new ApplyUserEnforcementResult(targetUserId, "ACTIVE", 0, false, false));

        var result = useCase.execute(new RestrictUserByAdminCommand(
                actorId, targetUserId, UUID.randomUUID(), "SPAM", "Spam", null
        ));

        assertEquals(targetUserId, result.userId());
        assertEquals("ACTIVE", result.status());
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
}
