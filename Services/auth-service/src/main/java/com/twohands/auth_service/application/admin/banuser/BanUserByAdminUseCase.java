package com.twohands.auth_service.application.admin.banuser;

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
public class BanUserByAdminUseCase {

    private static final String USER_BAN_PERMISSION = "USER_BAN";
    private static final String USER_SUSPEND_PERMISSION = "USER_SUSPEND";
    private static final String SUCCESS_MESSAGE = "Ban user thanh cong.";

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public BanUserByAdminUseCase(
            ApplyUserEnforcementUseCase applyUserEnforcementUseCase,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.applyUserEnforcementUseCase = applyUserEnforcementUseCase;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public BanUserByAdminResult execute(BanUserByAdminCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanBanUsers(actorAdminId);

        ApplyUserEnforcementResult applied = applyUserEnforcementUseCase.execute(ApplyUserEnforcementCommand.forSyncApply(
                command.enforcementId(),
                command.targetUserId(),
                UserEnforcementActionType.BAN,
                command.reasonCode(),
                command.description(),
                command.expiresAt()
        ));

        return new BanUserByAdminResult(
                applied.userId(),
                applied.status(),
                applied.revokedSessionCount()
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

    private void ensureActorCanBanUsers(UUID actorAdminId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
        if (authorizationDomainService.hasPermission(permissions, USER_BAN_PERMISSION)
                || authorizationDomainService.hasPermission(permissions, USER_SUSPEND_PERMISSION)) {
            return;
        }
        List<String> roles = permissionQueryRepository.findRoleCodesByUserId(actorAdminId);
        if (roles.stream().anyMatch(code -> "ADMIN".equalsIgnoreCase(code) || "SUPER_ADMIN".equalsIgnoreCase(code))) {
            return;
        }
        throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
    }
}
