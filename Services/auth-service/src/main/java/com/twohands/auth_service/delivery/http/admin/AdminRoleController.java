package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersCommand;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersResult;
import com.twohands.auth_service.application.rbac.assignrolestousers.AssignRolesToUsersUseCase;
import com.twohands.auth_service.application.rbac.revokerolefromuser.RevokeRoleFromUserCommand;
import com.twohands.auth_service.application.rbac.revokerolefromuser.RevokeRoleFromUserResult;
import com.twohands.auth_service.application.rbac.revokerolefromuser.RevokeRoleFromUserUseCase;
import com.twohands.auth_service.application.rbac.viewrolelist.ViewRoleListResult;
import com.twohands.auth_service.application.rbac.viewrolelist.ViewRoleListUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.request.AssignRolesToUsersRequest;
import com.twohands.auth_service.delivery.http.admin.response.AssignRolesToUsersResponse;
import com.twohands.auth_service.delivery.http.admin.response.RevokeRoleFromUserResponse;
import com.twohands.auth_service.delivery.http.admin.response.ViewRoleListResponse;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminRoleController {

    private final AssignRolesToUsersUseCase assignRolesToUsersUseCase;
    private final RevokeRoleFromUserUseCase revokeRoleFromUserUseCase;
    private final ViewRoleListUseCase viewRoleListUseCase;

    public AdminRoleController(
            AssignRolesToUsersUseCase assignRolesToUsersUseCase,
            RevokeRoleFromUserUseCase revokeRoleFromUserUseCase,
            ViewRoleListUseCase viewRoleListUseCase
    ) {
        this.assignRolesToUsersUseCase = assignRolesToUsersUseCase;
        this.revokeRoleFromUserUseCase = revokeRoleFromUserUseCase;
        this.viewRoleListUseCase = viewRoleListUseCase;
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
}
