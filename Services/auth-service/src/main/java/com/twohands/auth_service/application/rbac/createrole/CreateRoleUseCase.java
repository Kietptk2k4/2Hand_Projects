package com.twohands.auth_service.application.rbac.createrole;

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
import java.util.regex.Pattern;

@Service
public class CreateRoleUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Tao vai tro thanh cong.";
    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,31}$");

    private final RoleRepository roleRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public CreateRoleUseCase(
            RoleRepository roleRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public CreateRoleResult execute(CreateRoleCommand command) {
        UUID actorUserId = requireActor(command.actorUserId());
        ensureActorHasRoleManagementPermission(actorUserId);

        String code = normalizeCode(command.code());
        String name = normalizeName(command.name());

        if (SystemRolePolicy.isProtected(code)) {
            throw new AppException(
                    ErrorCode.CONFLICT,
                    ErrorCode.CONFLICT.defaultMessage(),
                    "code",
                    "RESERVED"
            );
        }

        roleRepository.findByCode(code).ifPresent(existing -> {
            throw new AppException(
                    ErrorCode.CONFLICT,
                    ErrorCode.CONFLICT.defaultMessage(),
                    "code",
                    "DUPLICATE"
            );
        });

        Instant now = Instant.now();
        Role role = new Role(UUID.randomUUID(), code, name, Set.of(), now, now);
        Role saved = roleRepository.save(role);

        return new CreateRoleResult(
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

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "code",
                    "REQUIRED"
            );
        }
        String normalized = code.trim().toUpperCase();
        if (!ROLE_CODE_PATTERN.matcher(normalized).matches()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "code",
                    "INVALID_FORMAT"
            );
        }
        return normalized;
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
            String normalized = name.trim();
            if (normalized.isBlank()) {
                throw new RbacDomainError("RBAC_ROLE_NAME_REQUIRED", "Role name is required");
            }
            return normalized;
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
