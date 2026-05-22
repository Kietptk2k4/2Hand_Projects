package com.twohands.commerce_service.unit.application.shop;

import com.twohands.commerce_service.application.shop.common.ShopVacationUpdatedOutboxService;
import com.twohands.commerce_service.application.shop.updateshopvacation.UpdateShopVacationCommand;
import com.twohands.commerce_service.application.shop.updateshopvacation.UpdateShopVacationUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationDraft;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationResult;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationSnapshot;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateShopVacationUseCaseTest {

    @Mock
    private UpdateShopVacationRepository updateShopVacationRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ShopVacationUpdatedOutboxService shopVacationUpdatedOutboxService;

    private UpdateShopVacationUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateShopVacationUseCase(
                updateShopVacationRepository,
                outboxEventRepository,
                shopVacationUpdatedOutboxService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldEnableVacationWithMessage() {
        when(updateShopVacationRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(snapshot(false, null)));
        when(updateShopVacationRepository.updateVacationSettings(any(UpdateShopVacationDraft.class), eq(now)))
                .thenReturn(updatedResult(true, "Shop nghi den 25/05"));
        when(shopVacationUpdatedOutboxService.build(any(), any(), any(), eq(true), any(), any()))
                .thenReturn(sampleOutbox());

        UpdateShopVacationResult result = useCase.execute(
                new UpdateShopVacationCommand(sellerId, true, "Shop nghi den 25/05")
        );

        assertThat(result.isVacation()).isTrue();
        assertThat(result.vacationMessage()).isEqualTo("Shop nghi den 25/05");
        assertThat(result.status()).isEqualTo(ShopStatus.ACTIVE);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }

    @Test
    void shouldDisableVacationAndClearMessage() {
        when(updateShopVacationRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(snapshot(true, "Old message")));
        when(updateShopVacationRepository.updateVacationSettings(any(UpdateShopVacationDraft.class), eq(now)))
                .thenReturn(updatedResult(false, null));
        when(shopVacationUpdatedOutboxService.build(any(), any(), any(), eq(false), eq(null), any()))
                .thenReturn(sampleOutbox());

        UpdateShopVacationResult result = useCase.execute(
                new UpdateShopVacationCommand(sellerId, false, null)
        );

        assertThat(result.isVacation()).isFalse();
        assertThat(result.vacationMessage()).isNull();
    }

    @Test
    void shouldKeepExistingMessageWhenEnablingWithoutNewMessage() {
        when(updateShopVacationRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(snapshot(false, "Existing notice")));
        when(updateShopVacationRepository.updateVacationSettings(
                new UpdateShopVacationDraft(shopId, true, "Existing notice"),
                now
        )).thenReturn(updatedResult(true, "Existing notice"));
        when(shopVacationUpdatedOutboxService.build(any(), any(), any(), eq(true), any(), any()))
                .thenReturn(sampleOutbox());

        UpdateShopVacationResult result = useCase.execute(
                new UpdateShopVacationCommand(sellerId, true, null)
        );

        assertThat(result.vacationMessage()).isEqualTo("Existing notice");
    }

    @Test
    void shouldRejectVacationMessageTooLong() {
        when(updateShopVacationRepository.findBySellerId(sellerId))
                .thenReturn(Optional.of(snapshot(false, null)));

        String longMessage = "x".repeat(501);
        assertThatThrownBy(() -> useCase.execute(
                new UpdateShopVacationCommand(sellerId, true, longMessage)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(updateShopVacationRepository, never()).updateVacationSettings(any(), any());
    }

    @Test
    void shouldRejectWhenSellerHasNoShop() {
        when(updateShopVacationRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new UpdateShopVacationCommand(sellerId, true, "Away")
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHOP_NOT_FOUND);
    }

    private UpdateShopVacationSnapshot snapshot(boolean isVacation, String message) {
        return new UpdateShopVacationSnapshot(
                shopId,
                sellerId,
                ShopStatus.ACTIVE,
                isVacation,
                message
        );
    }

    private UpdateShopVacationResult updatedResult(boolean isVacation, String message) {
        return new UpdateShopVacationResult(
                shopId,
                sellerId,
                ShopStatus.ACTIVE,
                isVacation,
                message,
                now
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ShopVacationUpdatedOutboxService.EVENT_TYPE,
                "shop:test:vacation:updated",
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
