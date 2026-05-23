package com.twohands.auth_service.application.admin.revokeadminsession;

import com.twohands.auth_service.domain.rbac.AdminPortalAccessPolicy;
import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
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
public class RevokeAdminSessionUseCase {

    private static final String ADMIN_SESSION_REVOKE_PERMISSION = "ADMIN_SESSION_REVOKE";
    private static final String SUCCESS_MESSAGE = "Thu hoi phien admin thanh cong.";

    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final UserRepository userRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public RevokeAdminSessionUseCase(
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            UserRepository userRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.userRepository = userRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional
    public RevokeAdminSessionResult execute(RevokeAdminSessionCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanRevokeSessions(actorAdminId);

        RefreshTokenSession session = refreshTokenSessionRepository.findById(command.sessionId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
                ));

        ensureTargetIsAdminUser(session.userId());

        Instant now = Instant.now();
        int revokedCount;
        if (command.revokeAllSessions()) {
            revokedCount = refreshTokenSessionRepository.revokeAllByUserId(session.userId());
        } else {
            revokedCount = refreshTokenSessionRepository.markRevokedIfActive(session.id(), now);
        }

        return new RevokeAdminSessionResult(
                session.userId(),
                session.id(),
                revokedCount,
                command.revokeAllSessions()
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

    private void ensureActorCanRevokeSessions(UUID actorAdminId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
        if (authorizationDomainService.hasPermission(permissions, ADMIN_SESSION_REVOKE_PERMISSION)) {
            return;
        }
        List<String> roles = permissionQueryRepository.findRoleCodesByUserId(actorAdminId);
        if (roles.stream().anyMatch(code -> "ADMIN".equalsIgnoreCase(code) || "SUPER_ADMIN".equalsIgnoreCase(code))) {
            return;
        }
        throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
    }

    private void ensureTargetIsAdminUser(UUID targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
                ));
        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        List<String> roleCodes = permissionQueryRepository.findRoleCodesByUserId(targetUserId);
        Set<String> permissionCodes = permissionQueryRepository.findPermissionCodesByUserId(targetUserId);
        if (!AdminPortalAccessPolicy.canAccessAdminPortal(roleCodes, permissionCodes)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Target user is not an admin portal user");
        }
    }
}
