package com.twohands.auth_service.application.admin.restrictuser;

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
public class RestrictUserByAdminUseCase {

    private static final String USER_RESTRICT_PERMISSION = "USER_RESTRICT";
    private static final String SUCCESS_MESSAGE = "Restrict user thanh cong.";

    private final UserRepository userRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public RestrictUserByAdminUseCase(
            UserRepository userRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.userRepository = userRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional(readOnly = true)
    public RestrictUserByAdminResult execute(RestrictUserByAdminCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanRestrictUsers(actorAdminId);
        validateRequest(command.reasonCode(), command.description(), command.expiresAt());

        User user = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
                ));
        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        return new RestrictUserByAdminResult(
                user.id(),
                user.status().name(),
                0
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

    private void ensureActorCanRestrictUsers(UUID actorAdminId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
        if (authorizationDomainService.hasPermission(permissions, USER_RESTRICT_PERMISSION)) {
            return;
        }
        List<String> roles = permissionQueryRepository.findRoleCodesByUserId(actorAdminId);
        if (roles.stream().anyMatch(code -> "ADMIN".equalsIgnoreCase(code) || "SUPER_ADMIN".equalsIgnoreCase(code))) {
            return;
        }
        throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
    }

    private void validateRequest(String reasonCode, String description, Instant expiresAt) {
        if (reasonCode == null || reasonCode.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "reason_code is required");
        }
        if (description == null || description.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "description is required");
        }
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "expires_at must be in the future for temporary restrict");
        }
    }
}
