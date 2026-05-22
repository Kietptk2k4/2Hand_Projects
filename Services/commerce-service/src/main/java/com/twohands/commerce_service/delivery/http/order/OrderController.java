package com.twohands.commerce_service.delivery.http.order;

import com.twohands.commerce_service.application.order.cancelorder.CancelOrderCommand;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderResult;
import com.twohands.commerce_service.application.order.cancelorder.CancelOrderUseCase;
import com.twohands.commerce_service.application.order.completeorder.CompleteOrderCommand;
import com.twohands.commerce_service.application.order.completeorder.CompleteOrderResponse;
import com.twohands.commerce_service.application.order.completeorder.CompleteOrderUseCase;
import com.twohands.commerce_service.application.order.confirmorderreceived.ConfirmOrderReceivedCommand;
import com.twohands.commerce_service.application.order.confirmorderreceived.ConfirmOrderReceivedUseCase;
import com.twohands.commerce_service.application.order.trackorderstatus.TrackOrderStatusCommand;
import com.twohands.commerce_service.application.order.trackorderstatus.TrackOrderStatusUseCase;
import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedResult;
import com.twohands.commerce_service.domain.order.OrderItemTrackingLine;
import com.twohands.commerce_service.domain.order.OrderStatusHistoryEntry;
import com.twohands.commerce_service.domain.order.TrackOrderStatusResult;
import com.twohands.commerce_service.domain.payment.OrderPaymentTracking;
import com.twohands.commerce_service.domain.payment.PaymentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentStatusHistoryEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentTrackingLine;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/orders")
public class OrderController {

    private static final String ADMIN_ROLE = "ADMIN";

    private final CancelOrderUseCase cancelOrderUseCase;
    private final CompleteOrderUseCase completeOrderUseCase;
    private final ConfirmOrderReceivedUseCase confirmOrderReceivedUseCase;
    private final TrackOrderStatusUseCase trackOrderStatusUseCase;

    public OrderController(
            CancelOrderUseCase cancelOrderUseCase,
            CompleteOrderUseCase completeOrderUseCase,
            ConfirmOrderReceivedUseCase confirmOrderReceivedUseCase,
            TrackOrderStatusUseCase trackOrderStatusUseCase
    ) {
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.completeOrderUseCase = completeOrderUseCase;
        this.confirmOrderReceivedUseCase = confirmOrderReceivedUseCase;
        this.trackOrderStatusUseCase = trackOrderStatusUseCase;
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<TrackOrderStatusResponse>> trackOrderStatus(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        TrackOrderStatusResult result = trackOrderStatusUseCase.execute(
                new TrackOrderStatusCommand(buyerId, orderId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                trackOrderStatusUseCase.successMessage(),
                toTrackResponse(result)
        ));
    }

    @PostMapping("/{orderId}/confirm-received")
    public ResponseEntity<ApiResponse<ConfirmOrderReceivedResponse>> confirmOrderReceived(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        ConfirmOrderReceivedResult result = confirmOrderReceivedUseCase.execute(
                new ConfirmOrderReceivedCommand(buyerId, orderId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                confirmOrderReceivedUseCase.successMessage(result.alreadyCompleted()),
                toConfirmResponse(result)
        ));
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<CompleteOrderHttpResponse>> completeOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) CompleteOrderRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser principal = resolveAuthenticatedUser(authentication);
        requireAdminRole(principal);

        String reason = request == null ? null : request.reason();
        CompleteOrderResponse result = completeOrderUseCase.execute(
                new CompleteOrderCommand(
                        orderId,
                        reason,
                        "ADMIN:COMPLETE_ORDER",
                        "ADMIN"
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                completeOrderUseCase.successMessage(result.alreadyCompleted()),
                new CompleteOrderHttpResponse(
                        result.orderId(),
                        result.orderStatus(),
                        result.completedAt()
                )
        ));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody(required = false) CancelOrderRequest request,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        String reason = request == null ? null : request.reason();

        CancelOrderResult result = cancelOrderUseCase.execute(
                new CancelOrderCommand(buyerId, orderId, reason)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                cancelOrderUseCase.successMessage(result.alreadyCancelled()),
                new CancelOrderResponse(result.orderId(), result.status(), result.cancelledAt())
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        return resolveAuthenticatedUser(authentication).userId();
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    private void requireAdminRole(AuthenticatedUser principal) {
        if (principal.roles() == null || principal.roles().stream().noneMatch(ADMIN_ROLE::equalsIgnoreCase)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Admin role is required to complete an order");
        }
    }

    private ConfirmOrderReceivedResponse toConfirmResponse(ConfirmOrderReceivedResult result) {
        return new ConfirmOrderReceivedResponse(
                result.orderId(),
                result.orderStatus(),
                result.paymentStatus(),
                result.itemsCompleted(),
                result.paymentMarkedPaid(),
                result.orderCompleted()
        );
    }

    private TrackOrderStatusResponse toTrackResponse(TrackOrderStatusResult result) {
        return new TrackOrderStatusResponse(
                result.orderId(),
                result.orderStatus(),
                result.orderPaymentStatus(),
                result.paymentMethod(),
                result.totalAmount(),
                result.finalAmount(),
                result.createdAt(),
                result.updatedAt(),
                result.completedAt(),
                result.orderCompleted(),
                result.paymentPaid(),
                result.allItemsCompleted(),
                result.anyShipmentDelivered(),
                result.anyItemDelivered(),
                toPaymentResponse(result.payment()),
                result.items().stream().map(this::toItemResponse).toList(),
                result.shipments().stream().map(this::toShipmentResponse).toList(),
                result.orderTimeline().stream().map(this::toOrderTimelineEntry).toList()
        );
    }

    private TrackOrderStatusResponse.PaymentTrackingResponse toPaymentResponse(OrderPaymentTracking payment) {
        if (payment == null) {
            return null;
        }
        return new TrackOrderStatusResponse.PaymentTrackingResponse(
                payment.paymentId(),
                payment.status(),
                payment.paymentMethod(),
                payment.paidAt(),
                payment.expiredAt(),
                payment.timeline().stream().map(this::toPaymentTimelineEntry).toList()
        );
    }

    private TrackOrderStatusResponse.PaymentStatusTimelineEntryResponse toPaymentTimelineEntry(
            PaymentStatusHistoryEntry entry
    ) {
        return new TrackOrderStatusResponse.PaymentStatusTimelineEntryResponse(
                entry.oldStatus(),
                entry.newStatus(),
                entry.occurredAt()
        );
    }

    private TrackOrderStatusResponse.OrderItemTrackingResponse toItemResponse(OrderItemTrackingLine item) {
        return new TrackOrderStatusResponse.OrderItemTrackingResponse(
                item.orderItemId(),
                item.productId(),
                item.sellerId(),
                item.productName(),
                item.quantity(),
                item.status(),
                item.shipmentId(),
                item.completedAt()
        );
    }

    private TrackOrderStatusResponse.ShipmentTrackingResponse toShipmentResponse(ShipmentTrackingLine shipment) {
        return new TrackOrderStatusResponse.ShipmentTrackingResponse(
                shipment.shipmentId(),
                shipment.sellerId(),
                shipment.status(),
                shipment.carrier(),
                shipment.trackingNumber(),
                shipment.shippedAt(),
                shipment.deliveredAt(),
                shipment.timeline().stream().map(this::toShipmentTimelineEntry).toList()
        );
    }

    private TrackOrderStatusResponse.ShipmentStatusTimelineEntryResponse toShipmentTimelineEntry(
            ShipmentStatusHistoryEntry entry
    ) {
        return new TrackOrderStatusResponse.ShipmentStatusTimelineEntryResponse(
                entry.oldStatus(),
                entry.newStatus(),
                entry.rawStatus(),
                entry.occurredAt()
        );
    }

    private TrackOrderStatusResponse.OrderStatusTimelineEntryResponse toOrderTimelineEntry(
            OrderStatusHistoryEntry entry
    ) {
        return new TrackOrderStatusResponse.OrderStatusTimelineEntryResponse(
                entry.oldStatus(),
                entry.newStatus(),
                entry.changedBy(),
                entry.note(),
                entry.occurredAt()
        );
    }
}
