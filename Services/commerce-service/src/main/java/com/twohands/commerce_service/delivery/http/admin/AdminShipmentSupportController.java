package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.shipment.viewshipmentsupport.ViewShipmentSupportDetailCommand;
import com.twohands.commerce_service.application.shipment.viewshipmentsupport.ViewShipmentSupportDetailUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.shipment.ViewShipmentSupportDetailResponse;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportDetailResult;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/support/shipments")
public class AdminShipmentSupportController {

    private final ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminShipmentSupportController(
            ViewShipmentSupportDetailUseCase viewShipmentSupportDetailUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewShipmentSupportDetailUseCase = viewShipmentSupportDetailUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
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

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
