package com.twohands.commerce_service.delivery.http.shipment;

import com.twohands.commerce_service.application.shipment.syncghnshipment.SyncGhnShipmentCommand;
import com.twohands.commerce_service.application.shipment.syncghnshipment.SyncGhnShipmentStatusUseCase;
import com.twohands.commerce_service.application.shipment.trackshipment.TrackShipmentCommand;
import com.twohands.commerce_service.application.shipment.trackshipment.TrackShipmentUseCase;
import com.twohands.commerce_service.application.shipment.viewshippingaddresssnapshot.ViewShippingAddressSnapshotCommand;
import com.twohands.commerce_service.application.shipment.viewshippingaddresssnapshot.ViewShippingAddressSnapshotUseCase;
import com.twohands.commerce_service.application.shipment.viewshipment.ViewShipmentCommand;
import com.twohands.commerce_service.application.shipment.viewshipment.ViewShipmentUseCase;
import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotResult;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ViewShipmentResult;
import com.twohands.commerce_service.delivery.http.seller.ShipmentOrderItemSummaryResponse;
import com.twohands.commerce_service.delivery.http.seller.ShippingAddressSnapshotResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/shipments")
public class ShipmentTrackingController {

    private final ViewShippingAddressSnapshotUseCase viewShippingAddressSnapshotUseCase;
    private final ViewShipmentUseCase viewShipmentUseCase;
    private final TrackShipmentUseCase trackShipmentUseCase;
    private final SyncGhnShipmentStatusUseCase syncGhnShipmentStatusUseCase;

    public ShipmentTrackingController(
            ViewShippingAddressSnapshotUseCase viewShippingAddressSnapshotUseCase,
            ViewShipmentUseCase viewShipmentUseCase,
            TrackShipmentUseCase trackShipmentUseCase,
            SyncGhnShipmentStatusUseCase syncGhnShipmentStatusUseCase
    ) {
        this.viewShippingAddressSnapshotUseCase = viewShippingAddressSnapshotUseCase;
        this.viewShipmentUseCase = viewShipmentUseCase;
        this.trackShipmentUseCase = trackShipmentUseCase;
        this.syncGhnShipmentStatusUseCase = syncGhnShipmentStatusUseCase;
    }

    @GetMapping("/{shipmentId}/address-snapshot")
    public ResponseEntity<ApiResponse<ViewShippingAddressSnapshotResponse>> viewShippingAddressSnapshot(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ViewShippingAddressSnapshotResult result = viewShippingAddressSnapshotUseCase.execute(
                new ViewShippingAddressSnapshotCommand(userId, shipmentId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewShippingAddressSnapshotUseCase.successMessage(),
                toAddressSnapshotResponse(result)
        ));
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<ApiResponse<ViewShipmentResponse>> viewShipment(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        ViewShipmentResult result = viewShipmentUseCase.execute(
                new ViewShipmentCommand(userId, shipmentId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewShipmentUseCase.successMessage(),
                toViewResponse(result)
        ));
    }

    @PostMapping("/{shipmentId}/ghn/sync")
    public ResponseEntity<ApiResponse<SyncGhnShipmentResponse>> syncGhnShipment(
            @PathVariable UUID shipmentId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        var result = syncGhnShipmentStatusUseCase.execute(
                new SyncGhnShipmentCommand(shipmentId, userId, true)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                syncGhnShipmentStatusUseCase.successMessage(),
                SyncGhnShipmentResponse.from(result)
        ));
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

    private ViewShippingAddressSnapshotResponse toAddressSnapshotResponse(
            ViewShippingAddressSnapshotResult result
    ) {
        ShipmentAddressSnapshot address = result.addressSnapshot();
        return new ViewShippingAddressSnapshotResponse(
                result.shipmentId(),
                result.snapshotId(),
                result.accessedAs(),
                result.createdAt(),
                new ShippingAddressSnapshotResponse(
                        address.receiverName(),
                        address.phone(),
                        address.provinceCode(),
                        address.districtCode(),
                        address.wardCode(),
                        address.addressDetail(),
                        address.fullAddress()
                )
        );
    }

    private ViewShipmentResponse toViewResponse(ViewShipmentResult result) {
        SellerShipmentRecord shipment = result.shipment();
        return new ViewShipmentResponse(
                shipment.shipmentId(),
                shipment.orderId(),
                shipment.sellerId(),
                result.accessedAs(),
                shipment.carrier(),
                shipment.shipmentType(),
                shipment.status(),
                shipment.ghnOrderCode(),
                shipment.trackingNumber(),
                shipment.shippingFee(),
                shipment.codAmount(),
                shipment.weightGram(),
                shipment.estimatedDeliveryDate(),
                shipment.shippedAt(),
                shipment.deliveredAt(),
                shipment.createdAt(),
                shipment.updatedAt(),
                new ShippingAddressSnapshotResponse(
                        result.addressSnapshot().receiverName(),
                        result.addressSnapshot().phone(),
                        result.addressSnapshot().provinceCode(),
                        result.addressSnapshot().districtCode(),
                        result.addressSnapshot().wardCode(),
                        result.addressSnapshot().addressDetail(),
                        result.addressSnapshot().fullAddress()
                ),
                result.orderItems().stream()
                        .map(item -> new ShipmentOrderItemSummaryResponse(
                                item.orderItemId(),
                                item.productNameSnapshot(),
                                item.quantity(),
                                item.status()
                        ))
                        .toList(),
                result.statusHistory().stream()
                        .map(ViewShipmentResponse.ShipmentStatusHistoryEntryResponse::from)
                        .toList()
        );
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
