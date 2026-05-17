package com.twohands.auth_service.application.rbac.viewrolelist;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ViewRoleListUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Lay danh sach role thanh cong.";

    private final RoleRepository roleRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public ViewRoleListUseCase(
            RoleRepository roleRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    public ViewRoleListResult execute(UUID actorUserId) {
        UUID userId = requireActor(actorUserId);
        ensureActorHasRoleManagementPermission(userId);

        return new ViewRoleListResult(
                roleRepository.findAll().stream()
                        .map(role -> new ViewRoleListResult.RoleData(
                                role.id(),
                                role.code(),
                                role.name(),
                                role.createdAt(),
                                role.updatedAt()
                        ))
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
