package com.twohands.commerce_service.application.moderation.lookup;

import com.twohands.commerce_service.domain.moderation.CommerceModerationLookupRepository;
import com.twohands.commerce_service.domain.moderation.ProductModerationOwner;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LookupProductModerationOwnerUseCase {

    private final CommerceModerationLookupRepository commerceModerationLookupRepository;

    public LookupProductModerationOwnerUseCase(CommerceModerationLookupRepository commerceModerationLookupRepository) {
        this.commerceModerationLookupRepository = commerceModerationLookupRepository;
    }

    @Transactional(readOnly = true)
    public ProductModerationOwner execute(UUID productId) {
        return commerceModerationLookupRepository.findProductOwner(productId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage()));
    }
}
