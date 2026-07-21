package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.rbac.assignpermissiontorole.AssignPermissionToRoleCommand;
import com.twohands.auth_service.application.rbac.assignpermissiontorole.AssignPermissionToRoleResult;
import com.twohands.auth_service.application.rbac.assignpermissiontorole.AssignPermissionToRoleUseCase;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersCommand;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersResult;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersUseCase;
import com.twohands.auth_service.application.rbac.checkuserpermission.CheckUserPermissionResult;
import com.twohands.auth_service.application.rbac.checkuserpermission.CheckUserPermissionUseCase;
import com.twohands.auth_service.application.rbac.createrole.CreateRoleCommand;
import com.twohands.auth_service.application.rbac.createrole.CreateRoleResult;
import com.twohands.auth_service.application.rbac.createrole.CreateRoleUseCase;
import com.twohands.auth_service.application.rbac.deleterole.DeleteRoleCommand;
import com.twohands.auth_service.application.rbac.deleterole.DeleteRoleResult;
import com.twohands.auth_service.application.rbac.deleterole.DeleteRoleUseCase;
import com.twohands.auth_service.application.rbac.revokepermissionfromrole.RevokePermissionFromRoleCommand;
import com.twohands.auth_service.application.rbac.revokepermissionfromrole.RevokePermissionFromRoleResult;
import com.twohands.auth_service.application.rbac.revokepermissionfromrole.RevokePermissionFromRoleUseCase;
import com.twohands.auth_service.application.rbac.revokerolefromuser.RevokeRoleFromUserCommand;
import com.twohands.auth_service.application.rbac.revokerolefromuser.RevokeRoleFromUserResult;
import com.twohands.auth_service.application.rbac.revokerolefromuser.RevokeRoleFromUserUseCase;
import com.twohands.auth_service.application.rbac.viewpermissioncatalog.ViewPermissionCatalogResult;
import com.twohands.auth_service.application.rbac.viewpermissioncatalog.ViewPermissionCatalogUseCase;
import com.twohands.auth_service.application.rbac.viewpermissionsofrole.ViewPermissionsOfRoleResult;
import com.twohands.auth_service.application.rbac.viewpermissionsofrole.ViewPermissionsOfRoleUseCase;
import com.twohands.auth_service.application.rbac.viewrolelist.ViewRoleListResult;
import com.twohands.auth_service.application.rbac.viewrolelist.ViewRoleListUseCase;
import com.twohands.auth_service.application.rbac.viewuserlistforrbac.ViewUserListForRbacCommand;
import com.twohands.auth_service.application.rbac.viewuserlistforrbac.ViewUserListForRbacResult;
import com.twohands.auth_service.application.rbac.viewuserlistforrbac.ViewUserListForRbacUseCase;
import com.twohands.auth_service.application.rbac.updaterole.UpdateRoleCommand;
import com.twohands.auth_service.application.rbac.updaterole.UpdateRoleResult;
import com.twohands.auth_service.application.rbac.updaterole.UpdateRoleUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.request.AssignPermissionToRoleRequest;
import com.twohands.auth_service.delivery.http.admin.request.AssignRolesToUsersRequest;
import com.twohands.auth_service.delivery.http.admin.request.CreateRoleRequest;
import com.twohands.auth_service.delivery.http.admin.request.UpdateRoleRequest;
import com.twohands.auth_service.delivery.http.admin.response.AdminRoleResponse;
import com.twohands.auth_service.delivery.http.admin.response.AssignPermissionToRoleResponse;
import com.twohands.auth_service.delivery.http.admin.response.AssignRolesToUsersResponse;
import com.twohands.auth_service.delivery.http.admin.response.CheckUserPermissionResponse;
import com.twohands.auth_service.delivery.http.admin.response.DeleteRoleResponse;
import com.twohands.auth_service.delivery.http.admin.response.RevokePermissionFromRoleResponse;
import com.twohands.auth_service.delivery.http.admin.response.RevokeRoleFromUserResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewPermissionCatalogResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewPermissionsOfRoleResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewRoleListResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewUserListForRbacResponse;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminRoleController {

    private final AssignRolesToUsersUseCase assignRolesToUsersUseCase;
    private final AssignPermissionToRoleUseCase assignPermissionToRoleUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;
    private final RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase;
    private final ViewRoleListUseCase viewRoleListUseCase;
    private final ViewPermissionsOfRoleUseCase viewPermissionsOfRoleUseCase;
    private final ViewPermissionCatalogUseCase viewPermissionCatalogUseCase;
    private final CheckUserPermissionUseCase checkUserPermissionUseCase;
    private final ViewUserListForRbacUseCase viewUserListForRbacUseCase;
    private final CreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;

    public AdminRoleController(
            AssignRolesToUsersUseCase assignRolesToUsersUseCase,
            AssignPermissionToRoleUseCase assignPermissionToRoleUseCase,
            RevokeRoleFromUserUseCase revokeRoleFromUserUseCase,
            RevokePermissionFromRoleUseCase revokePermissionFromRoleUseCase,
            ViewRoleListUseCase viewRoleListUseCase,
            ViewPermissionsOfRoleUseCase viewPermissionsOfRoleUseCase,
            ViewPermissionCatalogUseCase viewPermissionCatalogUseCase,
            CheckUserPermissionUseCase checkUserPermissionUseCase,
            ViewUserListForRbacUseCase viewUserListForRbacUseCase,
            CreateRoleUseCase createRoleUseCase,
            UpdateRoleUseCase updateRoleUseCase,
            DeleteRoleUseCase deleteRoleUseCase
    ) {
        this.assignRolesToUsersUseCase = assignRolesToUsersUseCase;
        this.assignPermissionToRoleUseCase = assignPermissionToRoleUseCase;
        this.revokeRoleFromUserUseCase = revokeRoleFromUserUseCase;
        this.revokePermissionFromRoleUseCase = revokePermissionFromRoleUseCase;
        this.viewRoleListUseCase = viewRoleListUseCase;
        this.viewPermissionsOfRoleUseCase = viewPermissionsOfRoleUseCase;
        this.viewPermissionCatalogUseCase = viewPermissionCatalogUseCase;
        this.checkUserPermissionUseCase = checkUserPermissionUseCase;
        this.viewUserListForRbacUseCase = viewUserListForRbacUseCase;
        this.createRoleUseCase = createRoleUseCase;
        this.updateRoleUseCase = updateRoleUseCase;
        this.deleteRoleUseCase = deleteRoleUseCase;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<ViewUserListForRbacResponse>> viewUserListForRbac(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        ViewUserListForRbacResult result = viewUserListForRbacUseCase.execute(
                new ViewUserListForRbacCommand(actorUserId, status, q, sort, page, size)
        );

        ViewUserListForRbacResponse response = new ViewUserListForRbacResponse(
                result.items().stream()
                        .map(item -> new ViewUserListForRbacResponse.ItemData(
                                item.id().toString(),
                                item.email(),
                                item.displayName(),
                                item.status(),
                                item.roleCodes(),
                                item.createdAt()
                        ))
                        .toList(),
                new ViewUserListForRbacResponse.PaginationData(
                        result.pagination().page(),
                        result.pagination().size(),
                        result.pagination().totalItems(),
                        result.pagination().totalPages(),
                        result.pagination().hasNext()
                )
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewUserListForRbacUseCase.successMessage(), response));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<ViewRoleListResponse>> viewRoleList(Authentication authentication) {
        UUID actorUserId = extractUserId(authentication);
        ViewRoleListResult result = viewRoleListUseCase.execute(actorUserId);

        ViewRoleListResponse response = new ViewRoleListResponse(
                result.roles().stream()
                        .map(role -> new ViewRoleListResponse.RoleData(
                                role.id().toString(),
                                role.code(),
                                role.name(),
                                role.createdAt(),
                                role.updatedAt()
                        ))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewRoleListUseCase.successMessage(), response));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<AdminRoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        CreateRoleResult result = createRoleUseCase.execute(
                new CreateRoleCommand(actorUserId, request.code(), request.name())
        );

        AdminRoleResponse response = toAdminRoleResponse(result.id(), result.code(), result.name(), result.createdAt(), result.updatedAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), createRoleUseCase.successMessage(), response));
    }

    @PatchMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<AdminRoleResponse>> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleRequest request,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID parsedRoleId = parseUuid(roleId, "roleId");

        UpdateRoleResult result = updateRoleUseCase.execute(
                new UpdateRoleCommand(actorUserId, parsedRoleId, request.name())
        );

        AdminRoleResponse response = toAdminRoleResponse(result.id(), result.code(), result.name(), result.createdAt(), result.updatedAt());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), updateRoleUseCase.successMessage(), response));
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<DeleteRoleResponse>> deleteRole(
            @PathVariable String roleId,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID parsedRoleId = parseUuid(roleId, "roleId");

        DeleteRoleResult result = deleteRoleUseCase.execute(
                new DeleteRoleCommand(actorUserId, parsedRoleId)
        );

        DeleteRoleResponse response = new DeleteRoleResponse(
                result.id().toString(),
                result.code()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), deleteRoleUseCase.successMessage(), response));
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<ViewPermissionsOfRoleResponse>> viewPermissionsOfRole(
            @PathVariable String roleId,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID parsedRoleId = parseUuid(roleId, "roleId");
        ViewPermissionsOfRoleResult result = viewPermissionsOfRoleUseCase.execute(actorUserId, parsedRoleId);

        ViewPermissionsOfRoleResponse response = new ViewPermissionsOfRoleResponse(
                new ViewPermissionsOfRoleResponse.RoleData(
                        result.role().id().toString(),
                        result.role().code(),
                        result.role().name()
                ),
                result.permissions().stream()
                        .map(permission -> new ViewPermissionsOfRoleResponse.PermissionData(
                                permission.code(),
                                permission.description()
                        ))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewPermissionsOfRoleUseCase.successMessage(), response));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<ViewPermissionCatalogResponse>> viewPermissionCatalog(Authentication authentication) {
        UUID actorUserId = extractUserId(authentication);
        ViewPermissionCatalogResult result = viewPermissionCatalogUseCase.execute(actorUserId);

        ViewPermissionCatalogResponse response = new ViewPermissionCatalogResponse(
                result.permissions().stream()
                        .map(permission -> new ViewPermissionCatalogResponse.PermissionData(
                                permission.code(),
                                permission.description()
                        ))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), viewPermissionCatalogUseCase.successMessage(), response));
    }

    @PostMapping("/roles/{roleId}/permissions")
    public ResponseEntity<ApiResponse<AssignPermissionToRoleResponse>> assignPermissionToRole(
            @PathVariable String roleId,
            @Valid @RequestBody AssignPermissionToRoleRequest request,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID parsedRoleId = parseUuid(roleId, "roleId");

        AssignPermissionToRoleResult result = assignPermissionToRoleUseCase.execute(
                new AssignPermissionToRoleCommand(actorUserId, parsedRoleId, request.permissionCode())
        );

        AssignPermissionToRoleResponse response = new AssignPermissionToRoleResponse(
                result.roleId().toString(),
                result.permissionCode()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), assignPermissionToRoleUseCase.successMessage(), response));
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionCode}")
    public ResponseEntity<ApiResponse<RevokePermissionFromRoleResponse>> revokePermissionFromRole(
            @PathVariable String roleId,
            @PathVariable String permissionCode,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID parsedRoleId = parseUuid(roleId, "roleId");

        RevokePermissionFromRoleResult result = revokePermissionFromRoleUseCase.execute(
                new RevokePermissionFromRoleCommand(actorUserId, parsedRoleId, permissionCode)
        );

        RevokePermissionFromRoleResponse response = new RevokePermissionFromRoleResponse(
                result.roleId().toString(),
                result.permissionCode()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), revokePermissionFromRoleUseCase.successMessage(), response));
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<ApiResponse<CheckUserPermissionResponse>> checkUserPermission(
            @PathVariable String userId,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID targetUserId = parseUuid(userId, "userId");
        CheckUserPermissionResult result = checkUserPermissionUseCase.execute(actorUserId, targetUserId);

        CheckUserPermissionResponse response = new CheckUserPermissionResponse(
                result.userId().toString(),
                result.permissions().stream()
                        .map(permission -> new CheckUserPermissionResponse.PermissionData(permission.code()))
                        .toList()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), checkUserPermissionUseCase.successMessage(), response));
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<ApiResponse<AssignRolesToUsersResponse>> assignRole(
            @PathVariable String userId,
            @Valid @RequestBody AssignRolesToUsersRequest request,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID targetUserId = parseUuid(userId, "userId");
        UUID roleId = parseUuid(request.roleId(), "role_id");

        AssignRolesToUsersResult result = assignRolesToUsersUseCase.execute(
                new AssignRolesToUsersCommand(actorUserId, targetUserId, roleId)
        );

        AssignRolesToUsersResponse response = new AssignRolesToUsersResponse(
                result.userId().toString(),
                result.roleId().toString()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), assignRolesToUsersUseCase.successMessage(), response));
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<RevokeRoleFromUserResponse>> revokeRole(
            @PathVariable String userId,
            @PathVariable String roleId,
            Authentication authentication
    ) {
        UUID actorUserId = extractUserId(authentication);
        UUID targetUserId = parseUuid(userId, "userId");
        UUID parsedRoleId = parseUuid(roleId, "roleId");

        RevokeRoleFromUserResult result = revokeRoleFromUserUseCase.execute(
                new RevokeRoleFromUserCommand(actorUserId, targetUserId, parsedRoleId)
        );

        RevokeRoleFromUserResponse response = new RevokeRoleFromUserResponse(
                result.userId().toString(),
                result.roleId().toString()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), revokeRoleFromUserUseCase.successMessage(), response));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
        try {
            return UUID.fromString(authentication.getPrincipal().toString());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
    }

    private UUID parseUuid(String rawValue, String field) {
        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Du lieu khong hop le.", field, "INVALID_FORMAT");
        }
    }

    private AdminRoleResponse toAdminRoleResponse(
            UUID id,
            String code,
            String name,
            java.time.Instant createdAt,
            java.time.Instant updatedAt
    ) {
        return new AdminRoleResponse(
                id.toString(),
                code,
                name,
                createdAt,
                updatedAt
        );
    }
}
