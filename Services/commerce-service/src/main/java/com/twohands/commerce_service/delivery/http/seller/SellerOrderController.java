package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.order.viewsellerorders.ViewSellerOrdersCommand;
import com.twohands.commerce_service.application.order.viewsellerorders.ViewSellerOrdersUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.SellerOrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.SellerOrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/orders")
public class SellerOrderController {

    private final ViewSellerOrdersUseCase viewSellerOrdersUseCase;

    public SellerOrderController(ViewSellerOrdersUseCase viewSellerOrdersUseCase) {
        this.viewSellerOrdersUseCase = viewSellerOrdersUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewSellerOrdersResponse>> viewSellerOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(name = "shipment_status", required = false) String shipmentStatus,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerOrdersResult result = viewSellerOrdersUseCase.execute(
                new ViewSellerOrdersCommand(sellerId, page, limit, status, shipmentStatus)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerOrdersUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private ViewSellerOrdersResponse toResponse(ViewSellerOrdersResult result) {
        PageMeta pagination = result.pagination();
        return new ViewSellerOrdersResponse(
                result.items().stream().map(this::toEntryResponse).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                )
        );
    }

    private SellerOrderListEntryResponse toEntryResponse(SellerOrderListEntry entry) {
        return new SellerOrderListEntryResponse(
                entry.orderItemId(),
                entry.orderId(),
                entry.productId(),
                entry.quantity(),
                entry.unitPriceSnapshot(),
                entry.finalPrice(),
                entry.shippingFeeAllocated(),
                entry.productNameSnapshot(),
                entry.imageSnapshot(),
                entry.itemStatus(),
                entry.itemCreatedAt(),
                entry.itemUpdatedAt(),
                entry.orderStatus(),
                entry.orderPaymentStatus(),
                entry.orderPaymentMethod(),
                entry.orderCreatedAt(),
                toPaymentResponse(entry.payment()),
                toShipmentResponse(entry.shipment())
        );
    }

    private SellerOrderListPaymentSummaryResponse toPaymentResponse(SellerOrderListPaymentSummary payment) {
        if (payment == null) {
            return null;
        }
        return new SellerOrderListPaymentSummaryResponse(
                payment.paymentId(),
                payment.status(),
                payment.paymentMethod(),
                payment.amount(),
                payment.currency()
        );
    }

    private SellerOrderListShipmentSummaryResponse toShipmentResponse(SellerOrderListShipmentSummary shipment) {
        if (shipment == null || shipment.shipmentId() == null) {
            return null;
        }
        return new SellerOrderListShipmentSummaryResponse(
                shipment.shipmentId(),
                shipment.status(),
                shipment.carrier(),
                shipment.trackingNumber(),
                shipment.deliveryAddressSummary()
        );
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
