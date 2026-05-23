package com.twohands.auth_service.application.admin.revokeuserenforcement;

import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementCommand;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementResult;
import com.twohands.auth_service.application.admin.applyuserenforcement.ApplyUserEnforcementUseCase;
import com.twohands.auth_service.domain.enforcement.UserEnforcementActionType;
import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RevokeUserEnforcementByAdminUseCase {

    private static final String USER_ENFORCEMENT_REVOKE_PERMISSION = "USER_ENFORCEMENT_REVOKE";
    private static final String SUCCESS_MESSAGE = "Revoke user enforcement thanh cong.";

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public RevokeUserEnforcementByAdminUseCase(
            ApplyUserEnforcementUseCase applyUserEnforcementUseCase,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.applyUserEnforcementUseCase = applyUserEnforcementUseCase;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public RevokeUserEnforcementByAdminResult execute(RevokeUserEnforcementByAdminCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanRevokeEnforcement(actorAdminId);

        ApplyUserEnforcementResult applied = applyUserEnforcementUseCase.execute(
                ApplyUserEnforcementCommand.forSyncRevoke(
                        command.enforcementId(),
                        command.userId(),
                        UserEnforcementActionType.REVOKE,
                        command.reason(),
                        command.note(),
                        command.reactivateUser()
                )
        );

        return new RevokeUserEnforcementByAdminResult(
                applied.userId(),
                applied.status(),
                applied.reactivated()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private UUID requireActor(UUID actorAdminId) {
        if (actorAdminId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return actorAdminId;
    }

    private void ensureActorCanRevokeEnforcement(UUID actorAdminId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
        if (authorizationDomainService.hasPermission(permissions, USER_ENFORCEMENT_REVOKE_PERMISSION)) {
            return;
        }
        List<String> roles = permissionQueryRepository.findRoleCodesByUserId(actorAdminId);
        if (roles.stream().anyMatch(code -> "ADMIN".equalsIgnoreCase(code) || "SUPER_ADMIN".equalsIgnoreCase(code))) {
            return;
        }
        throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
    }
}
