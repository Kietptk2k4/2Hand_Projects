package com.twohands.auth_service.application.admin.viewusersessionsforadmin;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.session.RefreshTokenSession;
import com.twohands.auth_service.domain.session.RefreshTokenSessionPage;
import com.twohands.auth_service.domain.session.RefreshTokenSessionRepository;
import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.domain.user.User;
import com.twohands.auth_service.domain.user.UserRepository;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ViewUserSessionsForAdminUseCase {

    private static final String USER_INVESTIGATION_READ_PERMISSION = "USER_INVESTIGATION_READ";
    private static final String SUCCESS_MESSAGE = "Lay danh sach phien dang nhap thanh cong.";

    private final UserRepository userRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final ViewUserSessionsForAdminQueryValidationService queryValidationService;
    private final AuthorizationDomainService authorizationDomainService;

    public ViewUserSessionsForAdminUseCase(
            UserRepository userRepository,
            RefreshTokenSessionRepository refreshTokenSessionRepository,
            PermissionQueryRepository permissionQueryRepository,
            ViewUserSessionsForAdminQueryValidationService queryValidationService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.queryValidationService = queryValidationService;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional(readOnly = true)
    public ViewUserSessionsForAdminResult execute(ViewUserSessionsForAdminCommand command) {
        UUID actorAdminId = requireActor(command.actorAdminId());
        ensureActorCanViewInvestigation(actorAdminId);

        User user = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new AppException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()
                ));
        if (user.status() == UserStatus.DELETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }

        int page = queryValidationService.validatePage(command.page());
        int limit = queryValidationService.validateLimit(command.limit());
        ViewUserSessionsForAdminQueryValidationService.SessionStatusFilter statusFilter =
                queryValidationService.validateStatus(command.status());

        int offset = (page - 1) * limit;
        SessionStatus repositoryStatusFilter = statusFilter.status();
        RefreshTokenSessionPage sessionPage = refreshTokenSessionRepository.findPageByUserId(
                command.targetUserId(),
                repositoryStatusFilter,
                limit,
                offset
        );

        List<ViewUserSessionsForAdminResult.SessionItem> sessions = sessionPage.sessions().stream()
                .map(this::mapSession)
                .toList();

        long totalItems = sessionPage.totalItems();
        boolean hasNext = (long) page * limit < totalItems;

        return new ViewUserSessionsForAdminResult(
                user.id(),
                sessions,
                new ViewUserSessionsForAdminResult.Pagination(page, limit, totalItems, hasNext)
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private ViewUserSessionsForAdminResult.SessionItem mapSession(RefreshTokenSession session) {
        return new ViewUserSessionsForAdminResult.SessionItem(
                session.id(),
                session.deviceId(),
                session.ipAddress(),
                session.userAgent(),
                session.status().name(),
                session.createdAt(),
                session.updatedAt()
        );
    }

    private UUID requireActor(UUID actorAdminId) {
        if (actorAdminId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return actorAdminId;
    }

    private void ensureActorCanViewInvestigation(UUID actorAdminId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorAdminId);
        if (!authorizationDomainService.hasPermission(permissions, USER_INVESTIGATION_READ_PERMISSION)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }
}
