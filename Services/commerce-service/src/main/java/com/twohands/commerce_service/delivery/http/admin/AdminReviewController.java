package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.admin.viewadminreviews.ViewAdminReviewsForModerationCommand;
import com.twohands.commerce_service.application.admin.viewadminreviews.ViewAdminReviewsForModerationUseCase;
import com.twohands.commerce_service.application.review.moderatereview.ModerateReviewCommand;
import com.twohands.commerce_service.application.review.moderatereview.ModerateReviewUseCase;
import com.twohands.commerce_service.domain.admin.ViewAdminReviewsForModerationResult;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.review.ModerateReviewResult;
import com.twohands.commerce_service.domain.review.ReviewModerationAction;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/reviews")
public class AdminReviewController {

    private final ViewAdminReviewsForModerationUseCase viewAdminReviewsForModerationUseCase;
    private final ModerateReviewUseCase moderateReviewUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminReviewController(
            ViewAdminReviewsForModerationUseCase viewAdminReviewsForModerationUseCase,
            ModerateReviewUseCase moderateReviewUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewAdminReviewsForModerationUseCase = viewAdminReviewsForModerationUseCase;
        this.moderateReviewUseCase = moderateReviewUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewAdminReviewsForModerationResponse>> listReviews(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_REVIEW_HIDE
        );

        ViewAdminReviewsForModerationResult result = viewAdminReviewsForModerationUseCase.execute(
                new ViewAdminReviewsForModerationCommand(page, limit, status, rating, q)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewAdminReviewsForModerationUseCase.successMessage(),
                AdminModerationListResponseMapper.toReviewListResponse(result)
        ));
    }

    @PostMapping("/{reviewId}/moderate")
    public ResponseEntity<ApiResponse<ModerateReviewResponse>> moderateReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ModerateReviewRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(admin, CommerceAdminAuthorization.PERMISSION_REVIEW_HIDE);

        ReviewModerationAction action = parseAction(request.action());
        ModerateReviewResult result = moderateReviewUseCase.execute(new ModerateReviewCommand(
                admin.userId(),
                reviewId,
                action,
                request.reason()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                moderateReviewUseCase.successMessage(action, result.alreadyModerated()),
                toResponse(result)
        ));
    }

    private ReviewModerationAction parseAction(String raw) {
        try {
            return ReviewModerationAction.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.INVALID_REVIEW_MODERATION, "Invalid moderation action: " + raw);
        }
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    private ModerateReviewResponse toResponse(ModerateReviewResult result) {
        return new ModerateReviewResponse(
                result.reviewId(),
                result.orderItemId(),
                result.sellerId(),
                result.buyerId(),
                result.rating(),
                result.status(),
                result.previousStatus(),
                result.alreadyModerated(),
                result.sellerRatingAvg(),
                result.sellerRatingCount(),
                result.moderatedAt()
        );
    }
}
