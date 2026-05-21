package com.twohands.commerce_service.application.shop.createshop;

import com.twohands.commerce_service.application.shop.common.ShopCreatedOutboxService;
import com.twohands.commerce_service.common.media.CommerceShopMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.CreateShopDraft;
import com.twohands.commerce_service.domain.shop.CreateShopPickupDraft;
import com.twohands.commerce_service.domain.shop.CreateShopRepository;
import com.twohands.commerce_service.domain.shop.CreateShopResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateShopUseCase {

    private static final int SHOP_NAME_MAX_LENGTH = 255;

    private final CreateShopRepository createShopRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShopCreatedOutboxService shopCreatedOutboxService;
    private final CommerceShopMediaUrlValidator shopMediaUrlValidator;
    private final Clock clock;

    public CreateShopUseCase(
            CreateShopRepository createShopRepository,
            OutboxEventRepository outboxEventRepository,
            ShopCreatedOutboxService shopCreatedOutboxService,
            CommerceShopMediaUrlValidator shopMediaUrlValidator,
            Clock clock
    ) {
        this.createShopRepository = createShopRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shopCreatedOutboxService = shopCreatedOutboxService;
        this.shopMediaUrlValidator = shopMediaUrlValidator;
        this.clock = clock;
    }

    @Transactional
    public CreateShopResult execute(CreateShopCommand command) {
        validatePayload(command);

        if (createShopRepository.existsBySellerId(command.sellerId())) {
            throw new AppException(ErrorCode.SHOP_ALREADY_EXISTS);
        }

        shopMediaUrlValidator.validateOptionalUrl("avatar_url", command.avatarUrl());
        shopMediaUrlValidator.validateOptionalUrl("cover_url", command.coverUrl());

        Instant now = clock.instant();
        CreateShopResult created = createShopRepository.create(
                new CreateShopDraft(
                        command.sellerId(),
                        command.shopName().trim(),
                        normalizeOptionalText(command.description()),
                        normalizeOptionalText(command.avatarUrl()),
                        normalizeOptionalText(command.coverUrl()),
                        command.pickupProfile()
                ),
                now
        );

        outboxEventRepository.save(shopCreatedOutboxService.build(
                created.shopId(),
                created.sellerId(),
                created.status(),
                now
        ));

        return created;
    }

    public String successMessage() {
        return "Tao shop thanh cong.";
    }

    private void validatePayload(CreateShopCommand command) {
        if (!StringUtils.hasText(command.shopName())) {
            throw fieldError("shop_name", "must not be blank");
        }
        if (command.shopName().trim().length() > SHOP_NAME_MAX_LENGTH) {
            throw fieldError("shop_name", "must be at most " + SHOP_NAME_MAX_LENGTH + " characters");
        }
        validatePickupProfile(command.pickupProfile());
    }

    private void validatePickupProfile(CreateShopPickupDraft pickup) {
        if (pickup == null) {
            return;
        }
        requireText(pickup.pickupName(), "pickup_name");
        requireText(pickup.phone(), "phone");
        requireText(pickup.provinceCode(), "province_code");
        requireText(pickup.districtCode(), "district_code");
        requireText(pickup.wardCode(), "ward_code");
        requireText(pickup.addressDetail(), "address_detail");
    }

    private void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw fieldError(field, "must not be blank when pickup profile is provided");
        }
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
