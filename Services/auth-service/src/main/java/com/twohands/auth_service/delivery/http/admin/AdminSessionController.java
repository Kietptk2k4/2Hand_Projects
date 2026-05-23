package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.admin.revokeadminsession.RevokeAdminSessionCommand;
import com.twohands.auth_service.application.admin.revokeadminsession.RevokeAdminSessionResult;
import com.twohands.auth_service.application.admin.revokeadminsession.RevokeAdminSessionUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.request.RevokeAdminSessionRequest;
import com.twohands.auth_service.delivery.http.admin.response.RevokeAdminSessionResponse;
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
@RequestMapping("/api/v1/admin/sessions")
public class AdminSessionController {

    private final RevokeAdminSessionUseCase revokeAdminSessionUseCase;

    public AdminSessionController(RevokeAdminSessionUseCase revokeAdminSessionUseCase) {
        this.revokeAdminSessionUseCase = revokeAdminSessionUseCase;
    }

    @PostMapping("/{sessionId}/revoke")
    public ResponseEntity<ApiResponse<RevokeAdminSessionResponse>> revoke(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) RevokeAdminSessionRequest request
    ) {
        RevokeAdminSessionResult result = revokeAdminSessionUseCase.execute(new RevokeAdminSessionCommand(
                resolveAuthenticatedUserId(),
                sessionId,
                request == null ? false : request.revokeAllSessionsOrDefault()
        ));

        RevokeAdminSessionResponse response = new RevokeAdminSessionResponse(
                result.targetAdminUserId(),
                result.sessionId(),
                result.revokedSessionCount(),
                result.revokeAllSessions()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        revokeAdminSessionUseCase.successMessage(),
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
