package com.twohands.auth_service.application.rbac.viewpermissionsofrole;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ViewPermissionsOfRoleUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Lay danh sach permission cua role thanh cong.";

    private final RoleRepository roleRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public ViewPermissionsOfRoleUseCase(
            RoleRepository roleRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    public ViewPermissionsOfRoleResult execute(UUID actorUserId, UUID roleId) {
        UUID userId = requireActor(actorUserId);
        ensureActorHasRoleManagementPermission(userId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));

        return new ViewPermissionsOfRoleResult(
                new ViewPermissionsOfRoleResult.RoleData(role.id(), role.code(), role.name()),
                permissionQueryRepository.findPermissionsByRoleId(role.id()).stream()
                        .map(permission -> new ViewPermissionsOfRoleResult.PermissionData(
                                permission.code(),
                                permission.description()
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
