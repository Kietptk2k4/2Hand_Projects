package com.twohands.commerce_service.application.moderation.lookup;

import com.twohands.commerce_service.domain.moderation.CommerceModerationLookupRepository;
import com.twohands.commerce_service.domain.moderation.ReviewModerationParties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LookupReviewModerationPartiesUseCase {

    private final CommerceModerationLookupRepository commerceModerationLookupRepository;

    public LookupReviewModerationPartiesUseCase(CommerceModerationLookupRepository commerceModerationLookupRepository) {
        this.commerceModerationLookupRepository = commerceModerationLookupRepository;
    }

    @Transactional(readOnly = true)
    public ReviewModerationParties execute(UUID reviewId) {
        return commerceModerationLookupRepository.findReviewParties(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
    }
}
