package com.twohands.commerce_service.delivery.http.review;

import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewCommand;
import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.review.CreateProductReviewResult;
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
@RequestMapping("/commerce/api/v1/reviews")
public class ReviewController {

    private final CreateProductReviewUseCase createProductReviewUseCase;

    public ReviewController(CreateProductReviewUseCase createProductReviewUseCase) {
        this.createProductReviewUseCase = createProductReviewUseCase;
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
