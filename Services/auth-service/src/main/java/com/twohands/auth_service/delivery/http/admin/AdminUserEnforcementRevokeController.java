package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminCommand;
import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminResult;
import com.twohands.auth_service.application.admin.revokeuserenforcement.RevokeUserEnforcementByAdminUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.request.RevokeUserEnforcementByAdminRequest;
import com.twohands.auth_service.delivery.http.admin.response.RevokeUserEnforcementByAdminResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/user-enforcements")
public class AdminUserEnforcementRevokeController {

    private final RevokeUserEnforcementByAdminUseCase revokeUserEnforcementByAdminUseCase;

    public AdminUserEnforcementRevokeController(RevokeUserEnforcementByAdminUseCase revokeUserEnforcementByAdminUseCase) {
        this.revokeUserEnforcementByAdminUseCase = revokeUserEnforcementByAdminUseCase;
    }

    @PostMapping("/{enforcementId}/revoke")
    public ResponseEntity<ApiResponse<RevokeUserEnforcementByAdminResponse>> revoke(
            @PathVariable UUID enforcementId,
            @RequestBody RevokeUserEnforcementByAdminRequest request
    ) {
        RevokeUserEnforcementByAdminResult result = revokeUserEnforcementByAdminUseCase.execute(
                new RevokeUserEnforcementByAdminCommand(
                        resolveAuthenticatedUserId(),
                        enforcementId,
                        request.userId(),
                        request.actionType(),
                        request.reactivateUser(),
                        request.note(),
                        request.reason()
                )
        );

        RevokeUserEnforcementByAdminResponse response = new RevokeUserEnforcementByAdminResponse(
                result.userId(),
                result.status(),
                result.reactivated()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        revokeUserEnforcementByAdminUseCase.successMessage(),
                        response
                ));
    }

    private UUID resolveAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return UUID.fromString(authentication.getName());
    }
}
