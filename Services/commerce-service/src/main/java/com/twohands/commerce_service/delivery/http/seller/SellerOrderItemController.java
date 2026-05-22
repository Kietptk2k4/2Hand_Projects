package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.order.processsellerorderitem.ProcessSellerOrderItemCommand;
import com.twohands.commerce_service.application.order.processsellerorderitem.ProcessSellerOrderItemUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.order.ProcessSellerOrderItemResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/order-items")
public class SellerOrderItemController {

    private final ProcessSellerOrderItemUseCase processSellerOrderItemUseCase;

    public SellerOrderItemController(ProcessSellerOrderItemUseCase processSellerOrderItemUseCase) {
        this.processSellerOrderItemUseCase = processSellerOrderItemUseCase;
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<ProcessSellerOrderItemsResponse>> processOrderItems(
            @RequestBody @Valid ProcessSellerOrderItemsRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ProcessSellerOrderItemResult result = processSellerOrderItemUseCase.execute(
                new ProcessSellerOrderItemCommand(sellerId, request.orderItemIds())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                processSellerOrderItemUseCase.successMessage(result.newlyProcessedCount()),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private ProcessSellerOrderItemsResponse toResponse(ProcessSellerOrderItemResult result) {
        return new ProcessSellerOrderItemsResponse(
                result.items().stream()
                        .map(item -> new ProcessSellerOrderItemsResponse.ProcessedOrderItemResponse(
                                item.orderItemId(),
                                item.orderId(),
                                item.status(),
                                item.productNameSnapshot(),
                                item.quantity(),
                                item.newlyProcessed()
                        ))
                        .toList(),
                result.newlyProcessedCount(),
                result.alreadyProcessingCount(),
                result.processedAt()
        );
    }
}
