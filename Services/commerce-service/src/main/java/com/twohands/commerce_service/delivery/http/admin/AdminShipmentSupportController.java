package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusCommand;
import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusResult;
import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusUseCase;
import com.twohands.commerce_service.application.shipment.viewshipmentsupport.ViewShipmentSupportDetailCommand;
import com.twohands.commerce_service.application.shipment.viewshipmentsupport.ViewShipmentSupportDetailUseCase;
import com.twohands.commerce_service.application.shipment.viewshipmentsupportlist.ViewShipmentSupportListQuery;
import com.twohands.commerce_service.application.shipment.viewshipmentsupportlist.ViewShipmentSupportListResult;
import com.twohands.commerce_service.application.shipment.viewshipmentsupportlist.ViewShipmentSupportListUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.shipment.ViewShipmentSupportDetailResponse;
import com.twohands.commerce_service.delivery.http.shipment.ViewShipmentSupportListResponse;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailResult;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/support/shipments")
public class AdminShipmentSupportController {

    private final ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase;
    private final ViewShipmentSupportListUseCase viewShipmentSupportListUseCase;
    private final AdminOverrideShipmentStatusUseCase adminOverrideShipmentStatusUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminShipmentSupportController(
            ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase,
            ViewShipmentSupportListUseCase viewShipmentSupportListUseCase,
            AdminOverrideShipmentStatusUseCase adminOverrideShipmentStatusUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewShipmentSupportDetailUseCase = viewShipmentSupportDetailUseCase;
        this.viewShipmentSupportListUseCase = viewShipmentSupportListUseCase;
        this.adminOverrideShipmentStatusUseCase = adminOverrideShipmentStatusUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewShipmentSupportListResponse>> listShipmentSupport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String carrier,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_SHIPMENT_SUPPORT_READ
        );

        ViewShipmentSupportListResult result = viewShipmentSupportListUseCase.execute(
                new ViewShipmentSupportListQuery(status, carrier, sort, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewShipmentSupportListUseCase.successMessage(),
                ViewShipmentSupportListResponse.from(result)
        ));
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<ViewShipmentSupportDetailResponse>> viewShipmentSupportDetail(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_SHIPMENT_SUPPORT_READ
        );

        ViewShipmentSupportDetailResult result = viewShipmentSupportDetailUseCase.execute(
                new ViewShipmentSupportDetailCommand(shipmentId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewShipmentSupportDetailUseCase.successMessage(),
                ViewShipmentSupportDetailResponse.from(result)
        ));
    }

    @PatchMapping("/{shipmentId}/status")
    public ResponseEntity<ApiResponse<AdminOverrideShipmentStatusResponse>> overrideShipmentStatus(
            @PathVariable UUID shipmentId,
            @RequestBody AdminOverrideShipmentStatusRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_SHIPMENT_SUPPORT_WRITE
        );
        if (request.forceOrDefault()) {
            commerceAdminAuthorization.requirePermission(
                    admin,
                    CommerceAdminAuthorization.PERMISSION_SHIPMENT_SUPPORT_FORCE_WRITE
            );
        }

        AdminOverrideShipmentStatusResult result = adminOverrideShipmentStatusUseCase.execute(
                new AdminOverrideShipmentStatusCommand(
                        shipmentId,
                        request.status(),
                        request.reason(),
                        request.forceOrDefault()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                adminOverrideShipmentStatusUseCase.successMessage(result),
                AdminOverrideShipmentStatusResponse.from(result)
        ));
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
