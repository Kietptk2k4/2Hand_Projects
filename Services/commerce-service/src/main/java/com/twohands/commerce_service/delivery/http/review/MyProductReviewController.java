package com.twohands.commerce_service.delivery.http.review;

import com.twohands.commerce_service.application.review.viewmyproductreview.ViewMyProductReviewCommand;
import com.twohands.commerce_service.application.review.viewmyproductreview.ViewMyProductReviewUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.review.MyProductReviewSnapshot;
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
@RequestMapping("/commerce/api/v1/me/products")
public class MyProductReviewController {

    private final ViewMyProductReviewUseCase viewMyProductReviewUseCase;

    public MyProductReviewController(ViewMyProductReviewUseCase viewMyProductReviewUseCase) {
        this.viewMyProductReviewUseCase = viewMyProductReviewUseCase;
    }

    @GetMapping("/{productId}/review")
    public ResponseEntity<ApiResponse<ViewMyProductReviewResponse>> viewMyProductReview(
            @PathVariable UUID productId,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        MyProductReviewSnapshot snapshot = viewMyProductReviewUseCase.execute(
                new ViewMyProductReviewCommand(buyerId, productId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewMyProductReviewUseCase.successMessage(),
                ViewMyProductReviewResponse.from(snapshot)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
