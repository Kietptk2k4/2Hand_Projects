package com.twohands.auth_service.application.rbac.viewuserlistforrbac;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.domain.rbac.RbacUserListCriteria;
import com.twohands.auth_service.domain.rbac.RbacUserListPagedResult;
import com.twohands.auth_service.domain.rbac.RbacUserListRepository;
import com.twohands.auth_service.domain.rbac.RbacUserListSortField;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ViewUserListForRbacUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Lay danh sach user thanh cong.";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final RbacUserListRepository rbacUserListRepository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public ViewUserListForRbacUseCase(
            RbacUserListRepository rbacUserListRepository,
            PermissionQueryRepository permissionQueryRepository
    ) {
        this.rbacUserListRepository = rbacUserListRepository;
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    @Transactional(readOnly = true)
    public ViewUserListForRbacResult execute(ViewUserListForRbacCommand command) {
        UUID actorUserId = requireActor(command.actorUserId());
        ensureActorHasRoleManagementPermission(actorUserId);

        int page = validatePage(command.page());
        int size = validateSize(command.size());
        RbacUserListSortField sortField = ViewUserListForRbacQueryPolicy.parseSortField(command.sort());
        String status = ViewUserListForRbacQueryPolicy.normalizeStatus(command.status());
        String query = ViewUserListForRbacQueryPolicy.normalizeQuery(command.query());

        RbacUserListPagedResult pageResult = rbacUserListRepository.findPage(
                new RbacUserListCriteria(
                        Optional.ofNullable(status),
                        Optional.ofNullable(query),
                        sortField,
                        page,
                        size
                )
        );

        return new ViewUserListForRbacResult(
                pageResult.items().stream()
                        .map(item -> new ViewUserListForRbacResult.Item(
                                item.id(),
                                item.email(),
                                item.displayName(),
                                item.status(),
                                item.roleCodes(),
                                item.createdAt()
                        ))
                        .toList(),
                new ViewUserListForRbacResult.Pagination(
                        pageResult.page(),
                        pageResult.size(),
                        pageResult.totalItems(),
                        pageResult.totalPages(),
                        pageResult.hasNext()
                )
        );
    }

    public String successMessage() {
        return SUCCESS_MESSAGE;
    }

    private int validatePage(int page) {
        if (page < 1) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "page",
                    "INVALID_VALUE"
            );
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > MAX_SIZE) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "size",
                    "INVALID_VALUE"
            );
        }
        return size;
    }

    private UUID requireActor(UUID actorUserId) {
        if (actorUserId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        return actorUserId;
    }

    private void ensureActorHasRoleManagementPermission(UUID actorUserId) {
        Set<String> permissions = permissionQueryRepository.findPermissionCodesByUserId(actorUserId);
        if (!authorizationDomainService.hasPermission(permissions, REQUIRED_PERMISSION_CODE)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }
}
