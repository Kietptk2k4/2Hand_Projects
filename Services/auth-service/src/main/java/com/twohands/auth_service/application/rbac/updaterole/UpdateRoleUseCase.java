package com.twohands.auth_service.application.rbac.updaterole;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.RbacDomainError;
import com.twohands.auth_service.domain.rbac.Role;
import com.twohands.auth_service.domain.rbac.RoleRepository;
import com.twohands.auth_service.domain.rbac.SystemRolePolicy;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class UpdateRoleUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Cap nhat vai tro thanh cong.";

    private final RoleRepository roleRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public UpdateRoleUseCase(
            RoleRepository roleRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public UpdateRoleResult execute(UpdateRoleCommand command) {
        UUID actorUserId = requireActor(command.actorUserId());
        ensureActorHasRoleManagementPermission(actorUserId);

        Role role = loadRole(command.roleId());
        ensureRoleIsMutable(role);

        String name = normalizeName(command.name());
        Instant now = Instant.now();
        role.rename(name, now);
        Role saved = roleRepository.save(role);

        return new UpdateRoleResult(
                saved.id(),
                saved.code(),
                saved.name(),
                saved.createdAt(),
                saved.updatedAt()
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

    private void ensureRoleIsMutable(Role role) {
        if (SystemRolePolicy.isProtected(role.code())) {
            throw new AppException(
                    ErrorCode.FORBIDDEN,
                    "Khong the sua vai tro he thong.",
                    "code",
                    "SYSTEM_ROLE_PROTECTED"
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "name",
                    "REQUIRED"
            );
        }
        try {
            return name.trim();
        } catch (RbacDomainError error) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "name",
                    "REQUIRED"
            );
        }
    }
}
