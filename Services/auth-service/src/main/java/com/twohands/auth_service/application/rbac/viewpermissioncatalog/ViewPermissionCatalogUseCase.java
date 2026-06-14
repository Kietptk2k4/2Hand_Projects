package com.twohands.auth_service.application.rbac.viewpermissioncatalog;

import com.twohands.auth_service.domain.rbac.AuthorizationDomainService;
import com.twohands.auth_service.domain.rbac.PermissionQueryRepository;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ViewPermissionCatalogUseCase {

    private static final String REQUIRED_PERMISSION_CODE = "ASSIGN_ROLE";
    private static final String SUCCESS_MESSAGE = "Lay danh sach permission thanh cong.";

    private final PermissionQueryRepository permissionQueryRepository;
    private final AuthorizationDomainService authorizationDomainService;

    public ViewPermissionCatalogUseCase(PermissionQueryRepository permissionQueryRepository) {
        this.permissionQueryRepository = permissionQueryRepository;
        this.authorizationDomainService = new AuthorizationDomainService();
    }

    public ViewPermissionCatalogResult execute(UUID actorUserId) {
        ensureActorHasRoleManagementPermission(requireActor(actorUserId));

        return new ViewPermissionCatalogResult(
                permissionQueryRepository.findAllPermissions().stream()
                        .map(permission -> new ViewPermissionCatalogResult.PermissionData(
                                permission.code(),
                                permission.description()
                        ))
                        .toList()
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
}
