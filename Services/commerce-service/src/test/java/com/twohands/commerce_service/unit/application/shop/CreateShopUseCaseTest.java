package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.application.shop.common.ShopCreatedOutboxService;
import com.twohands.commerce_service.application.shop.createshop.CreateShopCommand;
import com.twohands.commerce_service.application.shop.createshop.CreateShopUseCase;
import com.twohands.commerce_service.common.media.CommerceShopMediaUrlValidator;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.CreateShopDraft;
import com.twohands.commerce_service.domain.shop.CreateShopPickupDraft;
import com.twohands.commerce_service.domain.shop.CreateShopRepository;
import com.twohands.commerce_service.domain.shop.CreateShopResult;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
class CreateShopUseCaseTest {

    @Mock
    private CreateShopRepository createShopRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ShopCreatedOutboxService shopCreatedOutboxService;

    @Mock
    private CommerceShopMediaUrlValidator shopMediaUrlValidator;

    @InjectMocks
    private CreateShopUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new CreateShopUseCase(
                createShopRepository,
                outboxEventRepository,
                shopCreatedOutboxService,
                shopMediaUrlValidator,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldCreateShopWithDefaultSettings() {
        when(createShopRepository.existsBySellerId(sellerId)).thenReturn(false);
        when(createShopRepository.create(any(CreateShopDraft.class), eq(now))).thenReturn(createdResult(false));
        when(shopCreatedOutboxService.build(any(), any(), any(), any())).thenReturn(sampleOutbox());

        CreateShopResult result = useCase.execute(new CreateShopCommand(
                sellerId,
                "My Shop",
                "Description",
                null,
                null,
                null
        ));

        assertThat(result.status()).isEqualTo(ShopStatus.ACTIVE);
        assertThat(result.vacationMode()).isFalse();
        assertThat(result.shippingProfileCreated()).isFalse();
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldCreateShopWithPickupProfile() {
        when(createShopRepository.existsBySellerId(sellerId)).thenReturn(false);
        when(createShopRepository.create(any(CreateShopDraft.class), eq(now))).thenReturn(createdResult(true));
        when(shopCreatedOutboxService.build(any(), any(), any(), any())).thenReturn(sampleOutbox());

        CreateShopResult result = useCase.execute(new CreateShopCommand(
                sellerId,
                "My Shop",
                null,
                null,
                null,
                pickupProfile()
        ));

        assertThat(result.shippingProfileCreated()).isTrue();
    }

    @Test
    void shouldRejectWhenSellerAlreadyHasShop() {
        when(createShopRepository.existsBySellerId(sellerId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CreateShopCommand(
                sellerId, "My Shop", null, null, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_ALREADY_EXISTS);

        verify(createShopRepository, never()).create(any(), any());
    }

    @Test
    void shouldRejectBlankShopName() {
        assertThatThrownBy(() -> useCase.execute(new CreateShopCommand(
                sellerId, "  ", null, null, null, null)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectIncompletePickupProfile() {
        assertThatThrownBy(() -> useCase.execute(new CreateShopCommand(
                sellerId,
                "My Shop",
                null,
                null,
                null,
                new CreateShopPickupDraft("Pickup", null, "79", "760", "26734", "Detail")
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void shouldRejectInvalidAvatarUrlWhenValidationEnabled() {
        when(createShopRepository.existsBySellerId(sellerId)).thenReturn(false);
        doThrow(new AppException(ErrorCode.INVALID_MEDIA_URL))
                .when(shopMediaUrlValidator).validateOptionalUrl(eq("avatar_url"), any());

        assertThatThrownBy(() -> useCase.execute(new CreateShopCommand(
                sellerId,
                "My Shop",
                null,
                "http://evil.example/avatar.png",
                null,
                null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MEDIA_URL);
    }

    private CreateShopPickupDraft pickupProfile() {
        return new CreateShopPickupDraft(
                "Pickup Name",
                "0901234567",
                "79",
                "760",
                "26734",
                "123 Street"
        );
    }

    private CreateShopResult createdResult(boolean shippingProfileCreated) {
        return new CreateShopResult(
                shopId,
                sellerId,
                "My Shop",
                "Description",
                null,
                null,
                ShopStatus.ACTIVE,
                false,
                shippingProfileCreated,
                now,
                now
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ShopCreatedOutboxService.EVENT_TYPE,
                "shop:" + shopId + ":created",
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
