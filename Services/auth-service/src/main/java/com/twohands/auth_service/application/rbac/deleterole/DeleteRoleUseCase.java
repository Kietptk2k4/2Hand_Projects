package com.twohands.auth_service.application.rbac.deleterole;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.domain.rbac.SystemRolePolicy;
import com.twohands.auth_service.domain.rbac.UserRoleAssignmentRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class DeleteRoleUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Xoa vai tro thanh cong.";

    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public DeleteRoleUseCase(
            RoleRepository roleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public DeleteRoleResult execute(DeleteRoleCommand command) {
        UUID actorUserId = requireActor(command.actorUserId());
        ensureActorHasRoleManagementPermission(actorUserId);

        Role role = loadRole(command.roleId());
        ensureRoleIsDeletable(role);

        UUID roleId = role.id();
        String roleCode = role.code();
        roleRepository.deleteById(roleId);

        return new DeleteRoleResult(roleId, roleCode);
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
        if (roleId == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "role_id",
                    "REQUIRED"
            );
        }
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
    }

    private void ensureRoleIsDeletable(Role role) {
        if (SystemRolePolicy.isProtected(role.code())) {
            throw new AppException(
                    ErrorCode.FORBIDDEN,
                    "Khong the xoa vai tro he thong.",
                    "code",
                    "SYSTEM_ROLE_PROTECTED"
            );
        }

        long assignedUsers = userRoleAssignmentRepository.countUsersByRoleId(role.id());
        if (assignedUsers > 0) {
            throw new AppException(
                    ErrorCode.CONFLICT,
                    ErrorCode.CONFLICT.defaultMessage(),
                    "role_id",
                    "IN_USE"
            );
        }
    }
}
