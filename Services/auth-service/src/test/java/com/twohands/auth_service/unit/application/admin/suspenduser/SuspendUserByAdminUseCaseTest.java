package com.twohands.auth_service.unit.application.admin.suspenduser;

import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementResult;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementUseCase;
import com.twohands.auth_service.application.admin.suspenduser.SuspendUserByAdminCommand;
import com.twohands.auth_service.application.admin.suspenduser.SuspendUserByAdminUseCase;
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

class SuspendUserByAdminUseCaseTest {

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase = Mockito.mock(ApplyUserEnforcementUseCase.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private SuspendUserByAdminUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new SuspendUserByAdminUseCase(applyUserEnforcementUseCase, permissionQueryRepository);
    }

    @Test
    void shouldDelegateSuspendToApplyUseCase() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_SUSPEND"));
        when(applyUserEnforcementUseCase.execute(any(ApplyUserEnforcementCommand.class)))
                .thenReturn(new ApplyUserEnforcementResult(targetUserId, "SUSPENDED", 2, false, false));

        var result = useCase.execute(new SuspendUserByAdminCommand(
                actorId,
                targetUserId,
                UUID.randomUUID(),
                "POLICY_VIOLATION",
                "Abuse",
                null
        ));

        assertEquals(targetUserId, result.userId());
        assertEquals("SUSPENDED", result.status());
        assertEquals(2, result.revokedSessionCount());
    }

    @Test
    void shouldRejectWhenActorLacksPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of());
        when(permissionQueryRepository.findRoleCodesByUserId(actorId)).thenReturn(List.of("MODERATOR"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new SuspendUserByAdminCommand(actorId, UUID.randomUUID(), UUID.randomUUID(), "SPAM", "Spam", null)
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }
}
