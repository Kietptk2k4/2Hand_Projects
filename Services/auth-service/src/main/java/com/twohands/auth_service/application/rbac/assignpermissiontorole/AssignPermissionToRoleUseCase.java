package com.twohands.auth_service.application.rbac.assignpermissiontorole;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.Permission;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.PermissionRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class AssignPermissionToRoleUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Gan permission cho role thanh cong.";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public AssignPermissionToRoleUseCase(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public AssignPermissionToRoleResult execute(AssignPermissionToRoleCommand command) {
        UUID actorUserId = requireActor(command.actorUserId());
        ensureActorHasRoleManagementPermission(actorUserId);

        String permissionCode = normalizePermissionCode(command.permissionCode());
        Role role = loadRole(command.roleId());
        Permission permission = loadPermission(permissionCode);

        if (role.hasPermission(permission.id())) {
            throw new AppException(
                    ErrorCode.CONFLICT,
                    ErrorCode.CONFLICT.defaultMessage(),
                    "permission_code",
                    "ALREADY_ASSIGNED"
            );
        }

        Instant now = Instant.now();
        role.assignPermission(permission.id(), now);
        roleRepository.save(role);

        return new AssignPermissionToRoleResult(role.id(), permission.code());
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

    private Role loadRole(UUID roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
    }

    private Permission loadPermission(String permissionCode) {
        return permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage(),
                        "permission_code",
                        "NOT_FOUND"
                ));
    }

    private String normalizePermissionCode(String permissionCode) {
        if (permissionCode == null || permissionCode.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "permission_code",
                    "REQUIRED"
            );
        }
        return permissionCode.trim();
    }
}
