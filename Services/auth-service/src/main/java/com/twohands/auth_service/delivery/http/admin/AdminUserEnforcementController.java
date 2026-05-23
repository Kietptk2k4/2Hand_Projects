package com.twohands.auth_service.delivery.http.admin;

import com.twohands.auth_service.application.admin.banuser.BanUserByAdminCommand;
import com.twohands.auth_service.application.admin.banuser.BanUserByAdminResult;
import com.twohands.auth_service.application.admin.banuser.BanUserByAdminUseCase;
import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminCommand;
import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminResult;
import com.twohands.auth_service.application.admin.restrictuser.RestrictUserByAdminUseCase;
import com.twohands.auth_service.application.admin.suspenduser.SuspendUserByAdminCommand;
import com.twohands.auth_service.application.admin.suspenduser.SuspendUserByAdminResult;
import com.twohands.auth_service.application.admin.suspenduser.SuspendUserByAdminUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.admin.request.SuspendUserByAdminRequest;
import com.twohands.auth_service.delivery.http.admin.response.SuspendUserByAdminResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserEnforcementController {

    private final SuspendUserByAdminUseCase suspendUserByAdminUseCase;
    private final BanUserByAdminUseCase banUserByAdminUseCase;
    private final RestrictUserByAdminUseCase restrictUserByAdminUseCase;

    public AdminUserEnforcementController(
            SuspendUserByAdminUseCase suspendUserByAdminUseCase,
            BanUserByAdminUseCase banUserByAdminUseCase,
            RestrictUserByAdminUseCase restrictUserByAdminUseCase
    ) {
        this.suspendUserByAdminUseCase = suspendUserByAdminUseCase;
        this.banUserByAdminUseCase = banUserByAdminUseCase;
        this.restrictUserByAdminUseCase = restrictUserByAdminUseCase;
    }

    @PostMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<SuspendUserByAdminResponse>> suspend(
            @PathVariable UUID userId,
            @Valid @RequestBody SuspendUserByAdminRequest request
    ) {
        SuspendUserByAdminResult result = suspendUserByAdminUseCase.execute(new SuspendUserByAdminCommand(
                resolveAuthenticatedUserId(),
                userId,
                request.enforcementId(),
                request.reasonCode(),
                request.description(),
                request.expiresAt()
        ));

        SuspendUserByAdminResponse response = new SuspendUserByAdminResponse(
                result.userId(),
                result.status(),
                result.revokedSessionCount()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        suspendUserByAdminUseCase.successMessage(),
                        response
                ));
    }

    @PostMapping("/{userId}/ban")
    public ResponseEntity<ApiResponse<SuspendUserByAdminResponse>> ban(
            @PathVariable UUID userId,
            @RequestBody SuspendUserByAdminRequest request
    ) {
        BanUserByAdminResult result = banUserByAdminUseCase.execute(new BanUserByAdminCommand(
                resolveAuthenticatedUserId(),
                userId,
                request.enforcementId(),
                request.reasonCode(),
                request.description(),
                request.expiresAt()
        ));

        SuspendUserByAdminResponse response = new SuspendUserByAdminResponse(
                result.userId(),
                result.status(),
                result.revokedSessionCount()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        banUserByAdminUseCase.successMessage(),
                        response
                ));
    }

    @PostMapping("/{userId}/restrict")
    public ResponseEntity<ApiResponse<SuspendUserByAdminResponse>> restrict(
            @PathVariable UUID userId,
            @RequestBody SuspendUserByAdminRequest request
    ) {
        RestrictUserByAdminResult result = restrictUserByAdminUseCase.execute(new RestrictUserByAdminCommand(
                resolveAuthenticatedUserId(),
                userId,
                request.enforcementId(),
                request.reasonCode(),
                request.description(),
                request.expiresAt()
        ));

        SuspendUserByAdminResponse response = new SuspendUserByAdminResponse(
                result.userId(),
                result.status(),
                result.revokedSessionCount()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        restrictUserByAdminUseCase.successMessage(),
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
