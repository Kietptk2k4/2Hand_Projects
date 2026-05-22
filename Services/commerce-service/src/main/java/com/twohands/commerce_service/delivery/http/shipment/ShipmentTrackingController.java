package com.twohands.commerce_service.delivery.http.shipment;

import com.twohands.commerce_service.application.shipment.trackshipment.TrackShipmentCommand;
import com.twohands.commerce_service.application.shipment.trackshipment.TrackShipmentUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.TrackShipmentResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/shipments")
public class ShipmentTrackingController {

    private final TrackShipmentUseCase trackShipmentUseCase;

    public ShipmentTrackingController(TrackShipmentUseCase trackShipmentUseCase) {
        this.trackShipmentUseCase = trackShipmentUseCase;
    }

    @GetMapping("/{shipmentId}/tracking")
    public ResponseEntity<ApiResponse<TrackShipmentResponse>> trackShipment(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        TrackShipmentResult result = trackShipmentUseCase.execute(
                new TrackShipmentCommand(userId, shipmentId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                trackShipmentUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private TrackShipmentResponse toResponse(TrackShipmentResult result) {
        return new TrackShipmentResponse(
                result.shipmentId(),
                result.orderId(),
                result.sellerId(),
                result.accessedAs(),
                result.status(),
                result.carrier(),
                result.shipmentType(),
                result.trackingNumber(),
                result.ghnOrderCode(),
                result.shippedAt(),
                result.deliveredAt(),
                result.estimatedDeliveryDate(),
                result.orderStatus(),
                result.shipmentDelivered(),
                result.orderCompleted(),
                result.timeline().stream().map(this::toTimelineEntry).toList()
        );
    }

    private TrackShipmentResponse.ShipmentStatusTimelineEntryResponse toTimelineEntry(
            ShipmentStatusHistoryEntry entry
    ) {
        return new TrackShipmentResponse.ShipmentStatusTimelineEntryResponse(
                entry.oldStatus(),
                entry.newStatus(),
                entry.rawStatus(),
                entry.occurredAt()
        );
    }
}
