package com.twohands.auth_service.application.admin.revokeuserenforcement;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RevokeUserEnforcementByAdminUseCase {

    private static final String USER_ENFORCEMENT_REVOKE_PERMISSION = "USER_ENFORCEMENT_REVOKE";
    private static final String SUCCESS_MESSAGE = "Revoke user enforcement thanh cong.";

    private final UserRepository userRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public RevokeUserEnforcementByAdminUseCase(
            UserRepository userRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.userRepository = userRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public RevokeUserEnforcementByAdminResult execute(RevokeUserEnforcementByAdminCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanRevokeEnforcement(actorAdminId);

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
                ));
        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        boolean reactivated = false;
        if (command.reactivateUser() && user.status() == UserStatus.SUSPENDED) {
            Instant now = Instant.now();
            user.reactivate(now);
            userRepository.updateStatus(user.id(), user.status(), user.updatedAt());
            reactivated = true;
        }

        return new RevokeUserEnforcementByAdminResult(
                user.id(),
                user.status().name(),
                reactivated
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
