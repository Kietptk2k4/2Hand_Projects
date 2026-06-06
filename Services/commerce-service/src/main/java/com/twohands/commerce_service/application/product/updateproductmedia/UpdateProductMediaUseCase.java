package com.twohands.commerce_service.application.product.updateproductmedia;

import com.twohands.commerce_service.application.product.common.ProductMediaUpdatedOutboxService;
import com.twohands.commerce_service.common.media.CommerceProductMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.product.ProductMediaItem;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.UpdateProductMediaProductRef;
import com.twohands.commerce_service.domain.product.UpdateProductMediaRepository;
import com.twohands.commerce_service.domain.product.UpdateProductMediaResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UpdateProductMediaUseCase {

    public static final String MEDIA_TYPE_IMAGE = "IMAGE";
    private static final int MAX_MEDIA_URL_LENGTH = 2048;

    private final UpdateProductMediaRepository updateProductMediaRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ProductMediaUpdatedOutboxService productMediaUpdatedOutboxService;
    private final CommerceProductMediaUrlValidator productMediaUrlValidator;
    private final Clock clock;

    public UpdateProductMediaUseCase(
            UpdateProductMediaRepository updateProductMediaRepository,
            OutboxEventRepository outboxEventRepository,
            ProductMediaUpdatedOutboxService productMediaUpdatedOutboxService,
            CommerceProductMediaUrlValidator productMediaUrlValidator,
            Clock clock
    ) {
        this.updateProductMediaRepository = updateProductMediaRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productMediaUpdatedOutboxService = productMediaUpdatedOutboxService;
        this.productMediaUrlValidator = productMediaUrlValidator;
        this.clock = clock;
    }

    @Transactional
    public UpdateProductMediaResult execute(UpdateProductMediaCommand command) {
        List<String> normalizedUrls = validateAndNormalize(command.mediaUrls());

        UpdateProductMediaProductRef product = updateProductMediaRepository
                .findProductByIdAndSellerId(command.productId(), command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.status() == ProductStatus.REMOVED) {
            throw new AppException(ErrorCode.PRODUCT_REMOVED, "Removed product media cannot be updated");
        }

        productMediaUrlValidator.validateProductMediaUrls(normalizedUrls);

        List<ProductMediaItem> mediaItems = new ArrayList<>(normalizedUrls.size());
        for (int index = 0; index < normalizedUrls.size(); index++) {
            mediaItems.add(new ProductMediaItem(normalizedUrls.get(index), MEDIA_TYPE_IMAGE, index));
        }

        List<ProductMediaItem> saved = updateProductMediaRepository.replaceMedia(product.productId(), mediaItems);

        Instant now = clock.instant();
        outboxEventRepository.save(productMediaUpdatedOutboxService.build(
                product.productId(),
                product.shopId(),
                product.sellerId(),
                product.status(),
                saved.size(),
                now
        ));

        List<String> savedUrls = saved.stream().map(ProductMediaItem::mediaUrl).toList();
        String thumbnailUrl = savedUrls.isEmpty() ? null : savedUrls.getFirst();

        return new UpdateProductMediaResult(
                product.productId(),
                product.sellerId(),
                product.shopId(),
                product.status(),
                thumbnailUrl,
                savedUrls,
                !savedUrls.isEmpty()
        );
    }

    public String successMessage() {
        return "Cap nhat hinh anh san pham thanh cong.";
    }

    private List<String> validateAndNormalize(List<String> mediaUrls) {
        if (mediaUrls == null) {
            throw fieldError("media_urls", "must not be null");
        }

        int maxCount = productMediaUrlValidator.maxProductMediaCount();
        if (mediaUrls.size() > maxCount) {
            throw fieldError("media_urls", "must contain at most " + maxCount + " items");
        }

        Set<String> seen = new HashSet<>();
        List<String> normalized = new ArrayList<>(mediaUrls.size());

        for (int index = 0; index < mediaUrls.size(); index++) {
            String raw = mediaUrls.get(index);
            String field = "media_urls[" + index + "]";

            if (!StringUtils.hasText(raw)) {
                throw fieldError(field, "must not be blank");
            }

            String url = raw.trim();
            if (url.length() > MAX_MEDIA_URL_LENGTH) {
                throw fieldError(field, "must be at most " + MAX_MEDIA_URL_LENGTH + " characters");
            }
            if (!seen.add(url)) {
                throw fieldError("media_urls", "duplicate media URL");
            }

            normalized.add(url);
        }

        return normalized;
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
