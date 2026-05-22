package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.application.shop.common.ShopUpdatedOutboxService;
import com.twohands.commerce_service.application.shop.updateshopprofile.UpdateShopProfileCommand;
import com.twohands.commerce_service.application.shop.updateshopprofile.UpdateShopProfileUseCase;
import com.twohands.commerce_service.common.media.CommerceShopMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileDraft;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileResult;
import com.twohands.commerce_service.domain.shop.UpdateShopProfileSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateShopProfileUseCaseTest {

    @Mock
    private UpdateShopProfileRepository updateShopProfileRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ShopUpdatedOutboxService shopUpdatedOutboxService;

    @Mock
    private CommerceShopMediaUrlValidator shopMediaUrlValidator;

    private UpdateShopProfileUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateShopProfileUseCase(
                updateShopProfileRepository,
                outboxEventRepository,
                shopUpdatedOutboxService,
                shopMediaUrlValidator,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldUpdateShopProfileForSeller() {
        when(updateShopProfileRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(existingSnapshot()));
        when(updateShopProfileRepository.updateProfile(any(UpdateShopProfileDraft.class), eq(now)))
                .thenReturn(updatedResult());
        when(shopUpdatedOutboxService.build(any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        UpdateShopProfileResult result = useCase.execute(
                new UpdateShopProfileCommand(sellerId, "New Shop", "New desc", null, null)
        );

        assertThat(result.shopName()).isEqualTo("New Shop");
        verify(shopMediaUrlValidator, never()).validateOptionalUrl(eq("avatar_url"), any());
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldAllowSuspendedShopToUpdateProfile() {
        when(updateShopProfileRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(new UpdateShopProfileSnapshot(
                        shopId,
                        sellerId,
                        "Old Shop",
                        null,
                        null,
                        null,
                        ShopStatus.SUSPENDED,
                        BigDecimal.ZERO,
                        0,
                        false,
                        now.minusSeconds(3600)
                )));
        when(updateShopProfileRepository.updateProfile(any(UpdateShopProfileDraft.class), eq(now)))
                .thenReturn(new UpdateShopProfileResult(
                        shopId,
                        sellerId,
                        "Renamed Shop",
                        null,
                        null,
                        null,
                        ShopStatus.SUSPENDED,
                        BigDecimal.ZERO,
                        0,
                        false,
                        now.minusSeconds(3600),
                        now
                ));
        when(shopUpdatedOutboxService.build(any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        UpdateShopProfileResult result = useCase.execute(
                new UpdateShopProfileCommand(sellerId, "Renamed Shop", null, null, null)
        );

        assertThat(result.shopName()).isEqualTo("Renamed Shop");
        assertThat(result.status()).isEqualTo(ShopStatus.SUSPENDED);
    }

    @Test
    void shouldRejectBlankShopName() {
        when(updateShopProfileRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(existingSnapshot()));

        assertThatThrownBy(() -> useCase.execute(
                new UpdateShopProfileCommand(sellerId, "   ", null, null, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateShopProfileRepository, never()).updateProfile(any(), any());
    }

    @Test
    void shouldRejectInvalidAvatarUrl() {
        when(updateShopProfileRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(existingSnapshot()));
        doThrow(new AppException(ErrorCode.INVALID_MEDIA_URL))
                .when(shopMediaUrlValidator)
                .validateOptionalUrl(eq("avatar_url"), any());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateShopProfileCommand(
                        sellerId,
                        null,
                        null,
                        "https://evil.example/avatar.png",
                        null
                )
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MEDIA_URL);
    }

    @Test
    void shouldRejectWhenNoFieldsProvided() {
        assertThatThrownBy(() -> useCase.execute(
                new UpdateShopProfileCommand(sellerId, null, null, null, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectWhenSellerHasNoShop() {
        when(updateShopProfileRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateShopProfileCommand(sellerId, "Shop", null, null, null)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_FOUND);
    }

    private UpdateShopProfileSnapshot existingSnapshot() {
        return new UpdateShopProfileSnapshot(
                shopId,
                sellerId,
                "Old Shop",
                "Old desc",
                null,
                null,
                ShopStatus.ACTIVE,
                new BigDecimal("4.50"),
                2,
                false,
                now.minusSeconds(3600)
        );
    }

    private UpdateShopProfileResult updatedResult() {
        return new UpdateShopProfileResult(
                shopId,
                sellerId,
                "New Shop",
                "New desc",
                null,
                null,
                ShopStatus.ACTIVE,
                new BigDecimal("4.50"),
                2,
                false,
                now.minusSeconds(3600),
                now
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ShopUpdatedOutboxService.EVENT_TYPE,
                "shop:test:updated",
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
