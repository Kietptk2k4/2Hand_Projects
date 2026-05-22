package com.twohands.commerce_service.application.shop.updateshopprofile;

import com.twohands.commerce_service.application.shop.common.ShopUpdatedOutboxService;
import com.twohands.commerce_service.common.media.CommerceShopMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileDraft;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileResult;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateShopProfileUseCase {

    private static final int SHOP_NAME_MAX_LENGTH = 255;

    private final UpdateShopProfileRepository updateShopProfileRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShopUpdatedOutboxService shopUpdatedOutboxService;
    private final CommerceShopMediaUrlValidator shopMediaUrlValidator;
    private final Clock clock;

    public UpdateShopProfileUseCase(
            UpdateShopProfileRepository updateShopProfileRepository,
            OutboxEventRepository outboxEventRepository,
            ShopUpdatedOutboxService shopUpdatedOutboxService,
            CommerceShopMediaUrlValidator shopMediaUrlValidator,
            Clock clock
    ) {
        this.updateShopProfileRepository = updateShopProfileRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shopUpdatedOutboxService = shopUpdatedOutboxService;
        this.shopMediaUrlValidator = shopMediaUrlValidator;
        this.clock = clock;
    }

    @Transactional
    public UpdateShopProfileResult execute(UpdateShopProfileCommand command) {
        validateHasUpdates(command);

        UpdateShopProfileSnapshot existing = updateShopProfileRepository
                .findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        String newShopName = resolveShopName(command.shopName(), existing.shopName());
        String newDescription = resolveOptionalText(command.description(), existing.description());
        String newAvatarUrl = resolveOptionalText(command.avatarUrl(), existing.avatarUrl());
        String newCoverUrl = resolveOptionalText(command.coverUrl(), existing.coverUrl());

        if (command.shopName() != null) {
            validateShopName(newShopName);
        }
        if (command.avatarUrl() != null) {
            shopMediaUrlValidator.validateOptionalUrl("avatar_url", newAvatarUrl);
        }
        if (command.coverUrl() != null) {
            shopMediaUrlValidator.validateOptionalUrl("cover_url", newCoverUrl);
        }

        Instant now = clock.instant();
        UpdateShopProfileResult updated = updateShopProfileRepository.updateProfile(
                new UpdateShopProfileDraft(
                        existing.shopId(),
                        existing.sellerId(),
                        newShopName,
                        newDescription,
                        newAvatarUrl,
                        newCoverUrl
                ),
                now
        );

        outboxEventRepository.save(shopUpdatedOutboxService.build(
                updated.shopId(),
                updated.sellerId(),
                updated.status(),
                now
        ));

        return updated;
    }

    public String successMessage() {
        return "Cap nhat thong tin shop thanh cong.";
    }

    private void validateHasUpdates(UpdateShopProfileCommand command) {
        if (command.shopName() == null
                && command.description() == null
                && command.avatarUrl() == null
                && command.coverUrl() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "request",
                    "at least one of shop_name, description, avatar_url, cover_url must be provided"
            );
        }
    }

    private void validateShopName(String shopName) {
        if (!StringUtils.hasText(shopName)) {
            throw fieldError("shop_name", "must not be blank");
        }
        if (shopName.length() > SHOP_NAME_MAX_LENGTH) {
            throw fieldError("shop_name", "must be at most " + SHOP_NAME_MAX_LENGTH + " characters");
        }
    }

    private String resolveShopName(String requested, String existing) {
        if (requested == null) {
            return existing;
        }
        return requested.trim();
    }

    private String resolveOptionalText(String requested, String existing) {
        if (requested == null) {
            return existing;
        }
        if (!StringUtils.hasText(requested)) {
            return null;
        }
        return requested.trim();
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
