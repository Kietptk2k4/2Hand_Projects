package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.order.viewsellerorderdetail.ViewSellerOrderDetailCommand;
import com.twohands.commerce_service.application.order.viewsellerorderdetail.ViewSellerOrderDetailUseCase;
import com.twohands.commerce_service.application.order.viewsellerorders.ViewSellerOrdersCommand;
import com.twohands.commerce_service.application.order.viewsellerorders.ViewSellerOrdersUseCase;
import com.twohands.commerce_service.domain.order.ViewSellerOrderDetailResult;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.order.SellerOrderListEntry;
import com.twohands.commerce_service.domain.order.SellerOrderListPaymentSummary;
import com.twohands.commerce_service.domain.order.SellerOrderListShipmentSummary;
import com.twohands.commerce_service.domain.order.ViewSellerOrdersResult;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/orders")
public class SellerOrderController {

    private final ViewSellerOrdersUseCase viewSellerOrdersUseCase;
    private final ViewSellerOrderDetailUseCase viewSellerOrderDetailUseCase;

    public SellerOrderController(
            ViewSellerOrdersUseCase viewSellerOrdersUseCase,
            ViewSellerOrderDetailUseCase viewSellerOrderDetailUseCase
    ) {
        this.viewSellerOrdersUseCase = viewSellerOrdersUseCase;
        this.viewSellerOrderDetailUseCase = viewSellerOrderDetailUseCase;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<ViewSellerOrderDetailResponse>> viewSellerOrderDetail(
            @PathVariable UUID orderId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerOrderDetailResult result = viewSellerOrderDetailUseCase.execute(
                new ViewSellerOrderDetailCommand(sellerId, orderId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerOrderDetailUseCase.successMessage(),
                toDetailResponse(result)
        ));
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

    private ViewSellerOrderDetailResponse toDetailResponse(ViewSellerOrderDetailResult result) {
        CommerceBuyerSummary buyer = result.buyer() == null
                ? CommerceBuyerSummary.empty()
                : result.buyer();
        return new ViewSellerOrderDetailResponse(
                result.orderId(),
                result.orderStatus(),
                result.orderPaymentStatus(),
                result.orderPaymentMethod(),
                result.orderCreatedAt(),
                toPaymentResponse(result.payment()),
                result.sellerItemsSubtotal(),
                result.sellerShippingTotal(),
                result.items().stream().map(this::toEntryResponse).toList(),
                toShippingAddressResponse(result.shippingAddress()),
                buyer.buyerId(),
                buyer.displayName(),
                buyer.avatarUrl()
        );
    }

    private ShippingAddressSnapshotResponse toShippingAddressResponse(ShipmentAddressSnapshot address) {
        if (address == null) {
            return null;
        }
        return new ShippingAddressSnapshotResponse(
                address.receiverName(),
                address.phone(),
                address.provinceCode(),
                address.districtCode(),
                address.wardCode(),
                address.addressDetail(),
                address.fullAddress()
        );
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
                entry.lineWeightGram(),
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
