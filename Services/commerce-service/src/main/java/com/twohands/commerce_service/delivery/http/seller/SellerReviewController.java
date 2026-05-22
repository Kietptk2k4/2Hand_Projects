package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.review.replytoreview.ReplyToReviewCommand;
import com.twohands.commerce_service.application.review.replytoreview.ReplyToReviewUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.review.ReplyToReviewResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/reviews")
public class SellerReviewController {

    private final ReplyToReviewUseCase replyToReviewUseCase;

    public SellerReviewController(ReplyToReviewUseCase replyToReviewUseCase) {
        this.replyToReviewUseCase = replyToReviewUseCase;
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ApiResponse<ReplyToReviewResponse>> replyToReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReplyToReviewRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ReplyToReviewResult result = replyToReviewUseCase.execute(
                new ReplyToReviewCommand(sellerId, reviewId, request.content())
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                replyToReviewUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private ReplyToReviewResponse toResponse(ReplyToReviewResult result) {
        return new ReplyToReviewResponse(
                result.replyId(),
                result.reviewId(),
                result.sellerId(),
                result.content(),
                result.createdAt()
        );
    }
}
