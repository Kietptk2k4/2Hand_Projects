package com.twohands.commerce_service.delivery.http.review;

import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewCommand;
import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewUseCase;
import com.twohands.commerce_service.application.review.updateproductreview.UpdateProductReviewCommand;
import com.twohands.commerce_service.application.review.updateproductreview.UpdateProductReviewUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.review.CreateProductReviewResult;
import com.twohands.commerce_service.domain.review.UpdateProductReviewResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/reviews")
public class ReviewController {

    private final CreateProductReviewUseCase createProductReviewUseCase;
    private final UpdateProductReviewUseCase updateProductReviewUseCase;

    public ReviewController(
            CreateProductReviewUseCase createProductReviewUseCase,
            UpdateProductReviewUseCase updateProductReviewUseCase
    ) {
        this.createProductReviewUseCase = createProductReviewUseCase;
        this.updateProductReviewUseCase = updateProductReviewUseCase;
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<UpdateProductReviewResponse>> updateProductReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid UpdateProductReviewRequest request,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        UpdateProductReviewResult result = updateProductReviewUseCase.execute(
                new UpdateProductReviewCommand(
                        buyerId,
                        reviewId,
                        request.rating(),
                        request.comment()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductReviewUseCase.successMessage(),
                toUpdateResponse(result)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateProductReviewResponse>> createProductReview(
            @RequestBody @Valid CreateProductReviewRequest request,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        CreateProductReviewResult result = createProductReviewUseCase.execute(new CreateProductReviewCommand(
                buyerId,
                request.orderItemId(),
                request.rating(),
                request.comment()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createProductReviewUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private UpdateProductReviewResponse toUpdateResponse(UpdateProductReviewResult result) {
        return new UpdateProductReviewResponse(
                result.reviewId(),
                result.orderItemId(),
                result.sellerId(),
                result.buyerId(),
                result.rating(),
                result.comment(),
                result.status(),
                result.ratingChanged(),
                result.createdAt(),
                result.updatedAt(),
                result.sellerRatingAvg(),
                result.sellerRatingCount()
        );
    }

    private CreateProductReviewResponse toResponse(CreateProductReviewResult result) {
        return new CreateProductReviewResponse(
                result.reviewId(),
                result.orderItemId(),
                result.sellerId(),
                result.buyerId(),
                result.rating(),
                result.comment(),
                result.status(),
                result.createdAt(),
                result.sellerRatingAvg(),
                result.sellerRatingCount()
        );
    }
}
