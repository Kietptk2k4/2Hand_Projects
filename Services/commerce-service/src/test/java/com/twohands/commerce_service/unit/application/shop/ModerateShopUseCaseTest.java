package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.application.shop.common.ShopClosedOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopRestoredOutboxService;
import com.twohands.commerce_service.application.shop.common.ShopSuspendedOutboxService;
import com.twohands.commerce_service.application.shop.moderateshop.ModerateShopCommand;
import com.twohands.commerce_service.application.shop.moderateshop.ModerateShopUseCase;
import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.ModerateShopRepository;
import com.twohands.commerce_service.domain.shop.ModerateShopResult;
import com.twohands.commerce_service.domain.shop.ShopForModeration;
import com.twohands.commerce_service.domain.shop.ShopModerationAction;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerateShopUseCaseTest {

    @Mock
    private ModerateShopRepository moderateShopRepository;

    @Mock
    private SyncCartItemStatusUseCase syncCartItemStatusUseCase;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ShopSuspendedOutboxService shopSuspendedOutboxService;

    @Mock
    private ShopClosedOutboxService shopClosedOutboxService;

    @Mock
    private ShopRestoredOutboxService shopRestoredOutboxService;

    private ModerateShopUseCase useCase;

    private final UUID adminId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ModerateShopUseCase(
                moderateShopRepository,
                syncCartItemStatusUseCase,
                outboxEventRepository,
                shopSuspendedOutboxService,
                shopClosedOutboxService,
                shopRestoredOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void suspendsActiveShopAndInvalidatesCart() {
        ShopForModeration shop = activeShop();
        when(moderateShopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(moderateShopRepository.updateStatus(shopId, ShopStatus.ACTIVE, ShopStatus.SUSPENDED, now))
                .thenReturn(true);
        when(syncCartItemStatusUseCase.syncBySellerId(sellerId))
                .thenReturn(new SyncCartItemStatusResult(3, 3, 0, 0));
        when(shopSuspendedOutboxService.build(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox(ShopSuspendedOutboxService.EVENT_TYPE));

        ModerateShopResult result = useCase.execute(new ModerateShopCommand(
                adminId, shopId, ShopModerationAction.SUSPEND, "Policy violation"
        ));

        assertThat(result.status()).isEqualTo(ShopStatus.SUSPENDED);
        assertThat(result.cartItemsInvalidated()).isEqualTo(3);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void restoresSuspendedShopAndSyncsCartItems() {
        ShopForModeration shop = new ShopForModeration(shopId, sellerId, "Shop", ShopStatus.SUSPENDED);
        when(moderateShopRepository.findById(shopId)).thenReturn(Optional.of(shop));
        when(moderateShopRepository.updateStatus(shopId, ShopStatus.SUSPENDED, ShopStatus.ACTIVE, now))
                .thenReturn(true);
        when(shopRestoredOutboxService.build(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox(ShopRestoredOutboxService.EVENT_TYPE));
        when(syncCartItemStatusUseCase.syncBySellerId(sellerId))
                .thenReturn(new SyncCartItemStatusResult(2, 1, 1, 0));

        ModerateShopResult result = useCase.execute(new ModerateShopCommand(
                adminId, shopId, ShopModerationAction.RESTORE, "Resolved"
        ));

        assertThat(result.status()).isEqualTo(ShopStatus.ACTIVE);
        assertThat(result.cartItemsInvalidated()).isEqualTo(1);
        verify(syncCartItemStatusUseCase).syncBySellerId(sellerId);
    }

    @Test
    void idempotentWhenAlreadySuspended() {
        ShopForModeration shop = new ShopForModeration(shopId, sellerId, "Shop", ShopStatus.SUSPENDED);
        when(moderateShopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        ModerateShopResult result = useCase.execute(new ModerateShopCommand(
                adminId, shopId, ShopModerationAction.SUSPEND, "Already suspended"
        ));

        assertThat(result.alreadyModerated()).isTrue();
        verify(moderateShopRepository, never()).updateStatus(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidTransition() {
        ShopForModeration shop = new ShopForModeration(shopId, sellerId, "Shop", ShopStatus.CLOSED);
        when(moderateShopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        assertThatThrownBy(() -> useCase.execute(new ModerateShopCommand(
                adminId, shopId, ShopModerationAction.SUSPEND, "Cannot suspend closed"
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SHOP_STATUS);
    }

    private ShopForModeration activeShop() {
        return new ShopForModeration(shopId, sellerId, "My Shop", ShopStatus.ACTIVE);
    }

    private OutboxEvent sampleOutbox(String eventType) {
        return new OutboxEvent(
                UUID.randomUUID(),
                eventType,
                "shop:test",
                shopId,
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
