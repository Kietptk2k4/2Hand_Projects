package com.twohands.commerce_service.unit.application.inventory;

import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryCommand;
import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryResult;
import com.twohands.commerce_service.application.inventory.reserveinventory.ReserveInventoryUseCase;
import com.twohands.commerce_service.domain.inventory.InventoryReservationLine;
import com.twohands.commerce_service.domain.inventory.ReserveInventoryRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReserveInventoryUseCaseTest {

    @Mock
    private ReserveInventoryRepository reserveInventoryRepository;

    @InjectMocks
    private ReserveInventoryUseCase useCase;

    private final UUID productId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @Test
    void shouldReserveInventoryInSortedProductOrder() {
        UUID productB = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID productA = UUID.fromString("00000000-0000-0000-0000-000000000001");

        ReserveInventoryResult result = useCase.execute(new ReserveInventoryCommand(
                List.of(
                        new InventoryReservationLine(productB, 2),
                        new InventoryReservationLine(productA, 1)
                ),
                now
        ));

        assertThat(result.reservedItems()).hasSize(2);
        assertThat(result.reservedItems().get(0).productId()).isEqualTo(productA);

        ArgumentCaptor<List<InventoryReservationLine>> linesCaptor = ArgumentCaptor.forClass(List.class);
        verify(reserveInventoryRepository).reserveAll(linesCaptor.capture(), eq(now));
        assertThat(linesCaptor.getValue()).extracting(InventoryReservationLine::productId)
                .containsExactly(productA, productB);
    }

    @Test
    void shouldMergeDuplicateProductLines() {
        ReserveInventoryResult result = useCase.execute(new ReserveInventoryCommand(
                List.of(
                        new InventoryReservationLine(productId, 2),
                        new InventoryReservationLine(productId, 3)
                ),
                now
        ));

        assertThat(result.reservedItems()).hasSize(1);
        assertThat(result.reservedItems().getFirst().quantity()).isEqualTo(5);
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThatThrownBy(() -> useCase.execute(new ReserveInventoryCommand(
                List.of(new InventoryReservationLine(productId, 0)),
                now
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}
