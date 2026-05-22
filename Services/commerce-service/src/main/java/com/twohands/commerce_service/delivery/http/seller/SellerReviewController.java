package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.review.replytoreview.ReplyToReviewCommand;
import com.twohands.commerce_service.application.review.replytoreview.ReplyToReviewUseCase;
import com.twohands.commerce_service.application.review.viewshopreviews.ViewShopReviewsCommand;
import com.twohands.commerce_service.application.review.viewshopreviews.ViewShopReviewsUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewMediaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewRatingSummaryResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewSellerReplyResponse;
import com.twohands.commerce_service.domain.review.ProductReviewSellerReply;
import com.twohands.commerce_service.domain.review.ReplyToReviewResult;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ShopReviewListItem;
import com.twohands.commerce_service.domain.review.ViewShopReviewsResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
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
@RequestMapping("/commerce/api/v1/seller/reviews")
public class SellerReviewController {

    private final ReplyToReviewUseCase replyToReviewUseCase;
    private final ViewShopReviewsUseCase viewShopReviewsUseCase;

    public SellerReviewController(
            ReplyToReviewUseCase replyToReviewUseCase,
            ViewShopReviewsUseCase viewShopReviewsUseCase
    ) {
        this.replyToReviewUseCase = replyToReviewUseCase;
        this.viewShopReviewsUseCase = viewShopReviewsUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewShopReviewsResponse>> viewShopReviews(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String status,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewShopReviewsResult result = viewShopReviewsUseCase.execute(
                new ViewShopReviewsCommand(sellerId, page, limit, rating, status)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewShopReviewsUseCase.successMessage(),
                toShopReviewsResponse(result)
        ));
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

    private ViewShopReviewsResponse toShopReviewsResponse(ViewShopReviewsResult result) {
        PageMeta pagination = result.pagination();
        return new ViewShopReviewsResponse(
                result.shopId(),
                new ProductReviewRatingSummaryResponse(
                        result.ratingSummary().ratingAvg(),
                        result.ratingSummary().ratingCount()
                ),
                result.reviews().stream().map(this::toShopReviewItemResponse).toList(),
                new PageMetaResponse(
                        pagination.page(),
                        pagination.limit(),
                        pagination.totalItems(),
                        pagination.totalPages(),
                        pagination.hasNext()
                )
        );
    }

    private ShopReviewItemResponse toShopReviewItemResponse(ShopReviewListItem item) {
        return new ShopReviewItemResponse(
                item.reviewId(),
                item.orderItemId(),
                item.productNameSnapshot(),
                item.rating(),
                item.comment(),
                item.status(),
                item.createdAt(),
                item.media().stream().map(this::toReviewMediaResponse).toList(),
                item.sellerReply() == null ? null : toSellerReplyResponse(item.sellerReply())
        );
    }

    private ProductReviewMediaResponse toReviewMediaResponse(ReviewMediaItem media) {
        return new ProductReviewMediaResponse(media.id(), media.url(), media.type());
    }

    private ProductReviewSellerReplyResponse toSellerReplyResponse(ProductReviewSellerReply reply) {
        return new ProductReviewSellerReplyResponse(reply.replyId(), reply.content(), reply.createdAt());
    }
}
