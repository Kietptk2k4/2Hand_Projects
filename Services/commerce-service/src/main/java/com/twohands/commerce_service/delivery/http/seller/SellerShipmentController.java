package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.shipment.cancelsellershipment.CancelSellerShipmentUseCase;
import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentCommand;
import com.twohands.commerce_service.application.shipment.createshipment.CreateShipmentUseCase;
import com.twohands.commerce_service.application.shipment.updatesellershipment.UpdateSellerShipmentCommand;
import com.twohands.commerce_service.application.shipment.viewghnprintlabel.ViewGhnPrintLabelUseCase;
import com.twohands.commerce_service.application.shipment.updatesellershipment.UpdateSellerShipmentUseCase;
import com.twohands.commerce_service.application.shipment.viewsellershipment.ViewSellerShipmentUseCase;
import com.twohands.commerce_service.application.shipment.viewsellershipments.ViewSellerShipmentsCommand;
import com.twohands.commerce_service.application.shipment.viewsellershipments.ViewSellerShipmentsUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.shipment.CreateShipmentResult;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/commerce/api/v1/seller/shipments")
public class SellerShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;
    private final UpdateSellerShipmentUseCase updateSellerShipmentUseCase;
    private final ViewSellerShipmentUseCase viewSellerShipmentUseCase;
    private final ViewSellerShipmentsUseCase viewSellerShipmentsUseCase;
    private final CancelSellerShipmentUseCase cancelSellerShipmentUseCase;
    private final ViewGhnPrintLabelUseCase viewGhnPrintLabelUseCase;

    public SellerShipmentController(
            CreateShipmentUseCase createShipmentUseCase,
            UpdateSellerShipmentUseCase updateSellerShipmentUseCase,
            ViewSellerShipmentUseCase viewSellerShipmentUseCase,
            ViewSellerShipmentsUseCase viewSellerShipmentsUseCase,
            CancelSellerShipmentUseCase cancelSellerShipmentUseCase,
            ViewGhnPrintLabelUseCase viewGhnPrintLabelUseCase
    ) {
        this.createShipmentUseCase = createShipmentUseCase;
        this.updateSellerShipmentUseCase = updateSellerShipmentUseCase;
        this.viewSellerShipmentUseCase = viewSellerShipmentUseCase;
        this.viewSellerShipmentsUseCase = viewSellerShipmentsUseCase;
        this.cancelSellerShipmentUseCase = cancelSellerShipmentUseCase;
        this.viewGhnPrintLabelUseCase = viewGhnPrintLabelUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewSellerShipmentsResponse>> viewShipments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerShipmentsResult result = viewSellerShipmentsUseCase.execute(
                new ViewSellerShipmentsCommand(sellerId, page, limit, status, q)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerShipmentsUseCase.successMessage(),
                SellerShipmentMapper.toListResponse(result)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateShipmentResponse>> createShipment(
            @RequestBody @Valid CreateShipmentRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        CreateShipmentResult result = createShipmentUseCase.execute(new CreateShipmentCommand(
                sellerId,
                request.orderId(),
                request.orderItemIds(),
                request.carrier(),
                request.shipmentType(),
                request.weightGram(),
                request.trackingNumber()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createShipmentUseCase.successMessage(),
                toCreateResponse(result)
        ));
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<SellerShipmentDetailResponse>> viewShipment(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerShipmentDetail detail = viewSellerShipmentUseCase.execute(sellerId, shipmentId);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "Lay thong tin shipment thanh cong.",
                SellerShipmentMapper.toDetailResponse(detail)
        ));
    }

    @PostMapping("/{shipmentId}/cancel")
    public ResponseEntity<ApiResponse<SellerShipmentDetailResponse>> cancelShipment(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerShipmentDetail detail = cancelSellerShipmentUseCase.execute(
                new CancelSellerShipmentUseCase.CancelSellerShipmentCommand(sellerId, shipmentId)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                cancelSellerShipmentUseCase.successMessage(),
                SellerShipmentMapper.toDetailResponse(detail)
        ));
    }

    @PostMapping("/{shipmentId}/ghn/cancel")
    public ResponseEntity<ApiResponse<SellerShipmentDetailResponse>> cancelGhnShipment(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        return cancelShipment(shipmentId, authentication);
    }

    @GetMapping("/{shipmentId}/ghn/print-label")
    public ResponseEntity<ApiResponse<ViewGhnPrintLabelResponse>> viewGhnPrintLabel(
            @PathVariable UUID shipmentId,
            @RequestParam(required = false, defaultValue = "a5") String format,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        var result = viewGhnPrintLabelUseCase.execute(
                new ViewGhnPrintLabelUseCase.ViewGhnPrintLabelCommand(sellerId, shipmentId, format)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewGhnPrintLabelUseCase.successMessage(),
                ViewGhnPrintLabelResponse.from(result)
        ));
    }

    @PatchMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<SellerShipmentDetailResponse>> updateShipment(
            @PathVariable UUID shipmentId,
            @RequestBody UpdateSellerShipmentRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerShipmentDetail detail = updateSellerShipmentUseCase.execute(new UpdateSellerShipmentCommand(
                sellerId,
                shipmentId,
                request.status(),
                request.trackingNumber()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateSellerShipmentUseCase.successMessage(),
                SellerShipmentMapper.toDetailResponse(detail)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private CreateShipmentResponse toCreateResponse(CreateShipmentResult result) {
        return new CreateShipmentResponse(
                result.shipmentId(),
                result.orderId(),
                result.sellerId(),
                result.carrier(),
                result.shipmentType(),
                result.status(),
                result.ghnOrderCode(),
                result.trackingNumber(),
                result.shippingFee(),
                result.codAmount(),
                result.weightGram(),
                result.estimatedDeliveryDate(),
                result.orderItemIds(),
                result.createdAt()
        );
    }
}
