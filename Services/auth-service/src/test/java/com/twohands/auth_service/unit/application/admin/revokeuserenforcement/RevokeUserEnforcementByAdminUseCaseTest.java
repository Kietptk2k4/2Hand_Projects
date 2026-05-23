package com.twohands.auth_service.unit.application.admin.revokeuserenforcement;

import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementResult;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementUseCase;
import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminCommand;
import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminUseCase;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RevokeUserEnforcementByAdminUseCaseTest {

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase = Mockito.mock(ApplyUserEnforcementUseCase.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private RevokeUserEnforcementByAdminUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new RevokeUserEnforcementByAdminUseCase(applyUserEnforcementUseCase, permissionQueryRepository);
    }

    @Test
    void shouldDelegateRevokeToApplyUseCase() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_ENFORCEMENT_REVOKE"));
        when(applyUserEnforcementUseCase.execute(any(ApplyUserEnforcementCommand.class)))
                .thenReturn(new ApplyUserEnforcementResult(targetUserId, "ACTIVE", 0, false, true));

        var result = useCase.execute(new RevokeUserEnforcementByAdminCommand(
                actorId,
                UUID.randomUUID(),
                targetUserId,
                "SUSPEND",
                true,
                "note",
                "reason"
        ));

        assertEquals(targetUserId, result.userId());
        assertTrue(result.reactivated());
    }

    @Test
    void shouldRejectWhenActorLacksPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of());
        when(permissionQueryRepository.findRoleCodesByUserId(actorId)).thenReturn(List.of("MODERATOR"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new RevokeUserEnforcementByAdminCommand(actorId, UUID.randomUUID(), UUID.randomUUID(), "RESTRICT", false, null, null)
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }
}
