package com.twohands.commerce_service.application.shop.moderateshop;

import com.twohands.commerce_service.application.shop.common.ShopClosedOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopRestoredOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopSuspendedOutboxService;
import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.ModerateShopRepository;
import com.twohands.commerce_service.domain.shop.ModerateShopResult;
import com.twohands.commerce_service.domain.shop.ShopForModeration;
import com.twohands.commerce_service.domain.shop.ShopModerationAction;
import com.twohands.commerce_service.domain.shop.ShopModerationPolicy;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class ModerateShopUseCase {

    private final ModerateShopRepository moderateShopRepository;
    private final SyncCartItemStatusUseCase syncCartItemStatusUseCase;
    private final OutboxEventRepository outboxEventRepository;
    private final ShopSuspendedOutboxService shopSuspendedOutboxService;
    private final ShopClosedOutboxService shopClosedOutboxService;
    private final ShopRestoredOutboxService shopRestoredOutboxService;
    private final Clock clock;

    public ModerateShopUseCase(
            ModerateShopRepository moderateShopRepository,
            SyncCartItemStatusUseCase syncCartItemStatusUseCase,
            OutboxEventRepository outboxEventRepository,
            ShopSuspendedOutboxService shopSuspendedOutboxService,
            ShopClosedOutboxService shopClosedOutboxService,
            ShopRestoredOutboxService shopRestoredOutboxService,
            Clock clock
    ) {
        this.moderateShopRepository = moderateShopRepository;
        this.syncCartItemStatusUseCase = syncCartItemStatusUseCase;
        this.outboxEventRepository = outboxEventRepository;
        this.shopSuspendedOutboxService = shopSuspendedOutboxService;
        this.shopClosedOutboxService = shopClosedOutboxService;
        this.shopRestoredOutboxService = shopRestoredOutboxService;
        this.clock = clock;
    }

    @Transactional
    public ModerateShopResult execute(ModerateShopCommand command) {
        validateReason(command.reason());

        ShopForModeration shop = moderateShopRepository.findById(command.shopId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        ShopStatus targetStatus = ShopModerationPolicy.targetStatus(command.action());
        Instant occurredAt = clock.instant();

        if (shop.status() == targetStatus) {
            return buildResult(shop, shop.status(), true, 0, occurredAt);
        }

        if (!ShopModerationPolicy.canTransition(shop.status(), targetStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_SHOP_STATUS,
                    "Invalid shop status transition from " + shop.status() + " to " + targetStatus
            );
        }

        boolean updated = moderateShopRepository.updateStatus(
                shop.shopId(),
                shop.status(),
                targetStatus,
                occurredAt
        );
        if (!updated) {
            throw new AppException(ErrorCode.INVALID_SHOP_STATUS, "Shop status changed concurrently");
        }

        int cartItemsInvalidated = 0;
        if (ShopModerationPolicy.shouldInvalidateCartItems(command.action(), false)
                || command.action() == ShopModerationAction.RESTORE) {
            SyncCartItemStatusResult cartSync = syncCartItemStatusUseCase.syncBySellerId(shop.sellerId());
            cartItemsInvalidated = cartSync.updated();
        }

        outboxEventRepository.save(buildModerationEvent(command, shop, targetStatus, occurredAt));

        return buildResult(shop, targetStatus, false, cartItemsInvalidated, occurredAt);
    }

    public String successMessage(ShopModerationAction action, boolean alreadyModerated) {
        if (alreadyModerated) {
            return "Shop da o trang thai yeu cau.";
        }
        return switch (action) {
            case SUSPEND -> "Suspend shop thanh cong.";
            case CLOSE -> "Dong shop thanh cong.";
            case RESTORE -> "Khoi phuc shop thanh cong.";
        };
    }

    private OutboxEvent buildModerationEvent(
            ModerateShopCommand command,
            ShopForModeration shop,
            ShopStatus newStatus,
            Instant occurredAt
    ) {
        return switch (command.action()) {
            case SUSPEND -> shopSuspendedOutboxService.build(
                    shop.shopId(),
                    shop.sellerId(),
                    command.adminId(),
                    shop.status(),
                    newStatus,
                    command.reason(),
                    occurredAt
            );
            case CLOSE -> shopClosedOutboxService.build(
                    shop.shopId(),
                    shop.sellerId(),
                    command.adminId(),
                    shop.status(),
                    newStatus,
                    command.reason(),
                    occurredAt
            );
            case RESTORE -> shopRestoredOutboxService.build(
                    shop.shopId(),
                    shop.sellerId(),
                    command.adminId(),
                    shop.status(),
                    newStatus,
                    command.reason(),
                    occurredAt
            );
        };
    }

    private ModerateShopResult buildResult(
            ShopForModeration shop,
            ShopStatus currentStatus,
            boolean alreadyModerated,
            int cartItemsInvalidated,
            Instant occurredAt
    ) {
        return new ModerateShopResult(
                shop.shopId(),
                shop.sellerId(),
                shop.shopName(),
                currentStatus,
                shop.status(),
                alreadyModerated,
                cartItemsInvalidated,
                occurredAt
        );
    }

    private void validateReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "reason is required", "reason", "must not be blank");
        }
    }
}
