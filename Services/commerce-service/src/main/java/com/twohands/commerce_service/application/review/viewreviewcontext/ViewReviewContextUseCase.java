package com.twohands.commerce_service.application.review.viewreviewcontext;

import com.twohands.commerce_service.domain.review.ReviewContextSnapshot;
import com.twohands.commerce_service.domain.review.ViewReviewContextRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewReviewContextUseCase {

    private final ViewReviewContextRepository viewReviewContextRepository;

    public ViewReviewContextUseCase(ViewReviewContextRepository viewReviewContextRepository) {
        this.viewReviewContextRepository = viewReviewContextRepository;
    }

    @Transactional(readOnly = true)
    public ReviewContextSnapshot execute(ViewReviewContextCommand command) {
        if (command.orderItemId() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "order_item_id",
                    "order_item_id is required"
            );
        }

        return viewReviewContextRepository
                .findByOrderItemIdAndBuyerId(command.orderItemId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    public String successMessage() {
        return "Lay thong tin danh gia thanh cong.";
    }
}
