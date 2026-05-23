package com.twohands.auth_service.unit.application.admin.banuser;

import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementResult;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementUseCase;
import com.twohands.auth_service.application.admin.banuser.BanUserByAdminCommand;
import com.twohands.auth_service.application.admin.banuser.BanUserByAdminUseCase;
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

class BanUserByAdminUseCaseTest {

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase = Mockito.mock(ApplyUserEnforcementUseCase.class);
    private final PermissionQueryRepository permissionQueryRepository = Mockito.mock(PermissionQueryRepository.class);

    private BanUserByAdminUseCase useCase;

    @BeforeEach
    void setup() {
        useCase = new BanUserByAdminUseCase(applyUserEnforcementUseCase, permissionQueryRepository);
    }

    @Test
    void shouldDelegateBanToApplyUseCase() {
        UUID actorId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();

        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of("USER_BAN"));
        when(applyUserEnforcementUseCase.execute(any(ApplyUserEnforcementCommand.class)))
                .thenReturn(new ApplyUserEnforcementResult(targetUserId, "SUSPENDED", 1, false, false));

        var result = useCase.execute(new BanUserByAdminCommand(
                actorId, targetUserId, UUID.randomUUID(), "FRAUD", "Fraud", null
        ));

        assertEquals(targetUserId, result.userId());
        assertEquals("SUSPENDED", result.status());
    }

    @Test
    void shouldRejectWhenActorLacksPermission() {
        UUID actorId = UUID.randomUUID();
        when(permissionQueryRepository.findPermissionCodesByUserId(actorId)).thenReturn(Set.of());
        when(permissionQueryRepository.findRoleCodesByUserId(actorId)).thenReturn(List.of("MODERATOR"));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(
                new BanUserByAdminCommand(actorId, UUID.randomUUID(), UUID.randomUUID(), "FRAUD", "Fraud", null)
        ));
        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }
}
