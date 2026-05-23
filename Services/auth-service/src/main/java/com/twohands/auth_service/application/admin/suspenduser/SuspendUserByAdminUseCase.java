package com.twohands.auth_service.application.admin.suspenduser;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
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
public class SuspendUserByAdminUseCase {

    private static final String USER_SUSPEND_PERMISSION = "USER_SUSPEND";
    private static final String SUCCESS_MESSAGE = "Suspend user thanh cong.";

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public SuspendUserByAdminUseCase(
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public SuspendUserByAdminResult execute(SuspendUserByAdminCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanSuspendUsers(actorAdminId);
        validateRequest(command.reasonCode(), command.description(), command.expiresAt());

        User user = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
                ));
        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        Instant now = Instant.now();
        if (user.status() != UserStatus.SUSPENDED) {
            user.suspend(now);
            userRepository.updateStatus(user.id(), user.status(), user.updatedAt());
        }

        int revokedSessionCount = refreshTokenSessionRepository.revokeAllByUserId(user.id());

        return new SuspendUserByAdminResult(
                user.id(),
                UserStatus.SUSPENDED.name(),
                revokedSessionCount
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

    private void ensureActorCanSuspendUsers(UUID actorAdminId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
        if (authorizationDomainService.hasPermission(permissions, USER_SUSPEND_PERMISSION)) {
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
            throw new AppException(ErrorCode.BAD_REQUEST, "expires_at must be in the future for temporary suspend");
        }
    }
}
