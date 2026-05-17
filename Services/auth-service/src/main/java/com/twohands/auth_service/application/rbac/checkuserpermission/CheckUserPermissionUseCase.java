package com.twohands.auth_service.application.rbac.checkuserpermission;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class CheckUserPermissionUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Lay danh sach permission cua user thanh cong.";

    private final UserRepository userRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public CheckUserPermissionUseCase(
            UserRepository userRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.userRepository = userRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    public CheckUserPermissionResult execute(UUID actorUserId, UUID targetUserId) {
        UUID validActorUserId = requireActor(actorUserId);
        ensureActorHasRoleManagementPermission(validActorUserId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
        if (targetUser.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        return new CheckUserPermissionResult(
                targetUser.id(),
                permissionQueryRepository.findPermissionCodesByUserId(targetUser.id()).stream()
                        .sorted()
                        .map(CheckUserPermissionResult.PermissionData::new)
                        .toList()
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private UUID requireActor(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return actorUserId;
    }

    private void ensureActorHasRoleManagementPermission(UUID actorUserId) {
        Set<String> actorPermissions = permissionQueryRepository.findPermissionCodesByUserId(actorUserId);
        if (!authorizationDomainService.hasPermission(actorPermissions, REQUIRED_PERMISSION_CODE)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }
}
