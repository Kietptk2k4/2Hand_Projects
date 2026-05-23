package com.twohands.auth_service.application.admin.viewloginhistoryforadmin;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.user.LoginLog;
import com.twohands.auth_service.domain.user.LoginLogPage;
import com.twohands.auth_service.domain.user.LoginLogQueryFilter;
import com.twohands.auth_service.domain.user.LoginLogRepository;
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
public class ViewLoginHistoryForAdminUseCase {

    private static final String USER_INVESTIGATION_READ_PERMISSION = "USER_INVESTIGATION_READ";
    private static final String SUCCESS_MESSAGE = "Lay lich su dang nhap thanh cong.";
    private static final int MAX_USER_AGENT_LENGTH = 512;

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final ViewLoginHistoryForAdminQueryValidationService queryValidationService;
    private final AuthorizationDomainService authorizationDomainService;

    public ViewLoginHistoryForAdminUseCase(
            UserRepository userRepository,
            LoginLogRepository loginLogRepository,
            PermissionQueryRepository permissionQueryRepository,
            ViewLoginHistoryForAdminQueryValidationService queryValidationService
    ) {
        this.userRepository = userRepository;
        this.loginLogRepository = loginLogRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.queryValidationService = queryValidationService;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional(readOnly = true)
    public ViewLoginHistoryForAdminResult execute(ViewLoginHistoryForAdminCommand command) {
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
        LoginLogQueryFilter filter = queryValidationService.validateFilters(
                command.success(),
                command.from(),
                command.to()
        );

        int offset = (page - 1) * limit;
        LoginLogPage logPage = loginLogRepository.findPageByUserId(command.targetUserId(), filter, limit, offset);

        List<ViewLoginHistoryForAdminResult.Item> items = logPage.items().stream()
                .map(this::mapItem)
                .toList();

        long totalItems = logPage.totalItems();
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + limit - 1) / limit);
        boolean hasNext = page < totalPages;

        return new ViewLoginHistoryForAdminResult(
                user.id(),
                items,
                new ViewLoginHistoryForAdminResult.Pagination(page, limit, totalItems, totalPages, hasNext)
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private ViewLoginHistoryForAdminResult.Item mapItem(LoginLog log) {
        return new ViewLoginHistoryForAdminResult.Item(
                log.loginMethod().name(),
                log.ipAddress(),
                truncateUserAgent(log.userAgent()),
                log.success(),
                log.createdAt()
        );
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        if (userAgent.length() <= MAX_USER_AGENT_LENGTH) {
            return userAgent;
        }
        return userAgent.substring(0, MAX_USER_AGENT_LENGTH);
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
