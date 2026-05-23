package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportResult;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.support.ViewWebhookLogsForSupportResponse;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/commerce/api/v1/admin/support/webhook-logs")
public class AdminWebhookSupportController {

    private final ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminWebhookSupportController(
            ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewWebhookLogsForSupportUseCase = viewWebhookLogsForSupportUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportResponse>> listWebhookLogs(
            @RequestParam(required = false) String provider,
            @RequestParam(name = "reference_id", required = false) String referenceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_WEBHOOK_SUPPORT_READ
        );

        ViewWebhookLogsForSupportResult result = viewWebhookLogsForSupportUseCase.execute(
                new ViewWebhookLogsForSupportQuery(provider, referenceId, status, from, to, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewWebhookLogsForSupportUseCase.successMessage(),
                ViewWebhookLogsForSupportResponse.from(result)
        ));
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
