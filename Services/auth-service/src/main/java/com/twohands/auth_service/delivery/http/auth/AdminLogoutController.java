package com.twohands.auth_service.delivery.http.auth;

import com.twohands.auth_service.application.auth.adminlogout.AdminLogoutCommand;
import com.twohands.auth_service.application.auth.adminlogout.AdminLogoutUseCase;
import com.twohands.auth_service.common.dto.ApiResponse;
import com.twohands.auth_service.delivery.http.auth.request.LogoutRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AdminLogoutController {

    private final AdminLogoutUseCase adminLogoutUseCase;

    public AdminLogoutController(AdminLogoutUseCase adminLogoutUseCase) {
        this.adminLogoutUseCase = adminLogoutUseCase;
    }

    @PostMapping("/admin/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpServletRequest
    ) {
        adminLogoutUseCase.execute(new AdminLogoutCommand(
                resolveAuthenticatedUserId(),
                request.refreshToken(),
                httpServletRequest.getRemoteAddr()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        HttpStatus.OK.value(),
                        adminLogoutUseCase.logoutSuccessMessage(),
                        null
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
