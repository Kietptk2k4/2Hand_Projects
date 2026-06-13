package com.twohands.commerce_service.application.review.viewmyproductreview;

import com.twohands.commerce_service.domain.review.MyProductReviewSnapshot;
import com.twohands.commerce_service.domain.review.ViewMyProductReviewRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ViewMyProductReviewUseCase {

    private final ViewMyProductReviewRepository viewMyProductReviewRepository;
    private final Clock clock;

    public ViewMyProductReviewUseCase(
            ViewMyProductReviewRepository viewMyProductReviewRepository,
            Clock clock
    ) {
        this.viewMyProductReviewRepository = viewMyProductReviewRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public MyProductReviewSnapshot execute(ViewMyProductReviewCommand command) {
        UUID productId = command.productId();
        if (!viewMyProductReviewRepository.isProductBuyerVisible(productId, clock.instant())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return viewMyProductReviewRepository
                .findBuyerReviewForProduct(command.buyerId(), productId)
                .map(snapshot -> withProductId(snapshot, productId))
                .orElseGet(() -> MyProductReviewSnapshot.noReview(productId));
    }

    public String successMessage() {
        return "Lay danh gia cua ban thanh cong.";
    }

    private MyProductReviewSnapshot withProductId(MyProductReviewSnapshot snapshot, UUID productId) {
        return new MyProductReviewSnapshot(
                snapshot.hasReview(),
                productId,
                snapshot.reviewId(),
                snapshot.orderItemId(),
                snapshot.rating(),
                snapshot.comment(),
                snapshot.status(),
                snapshot.createdAt(),
                snapshot.updatedAt(),
                snapshot.canEdit()
        );
    }
}
