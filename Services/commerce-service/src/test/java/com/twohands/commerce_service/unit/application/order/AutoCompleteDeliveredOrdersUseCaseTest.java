package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.autocompletedeliveredorder.AutoCompleteDeliveredOrdersResult;
import com.twohands.commerce_service.application.order.autocompletedeliveredorder.AutoCompleteDeliveredOrdersUseCase;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionRepository;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionResult;
import com.twohands.commerce_service.domain.order.StaleDeliveredOrderItemCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoCompleteDeliveredOrdersUseCaseTest {

    @Mock
    private DeliveredOrderCompletionRepository deliveredOrderCompletionRepository;

    private AutoCompleteDeliveredOrdersUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AutoCompleteDeliveredOrdersUseCase(deliveredOrderCompletionRepository, 20, 7);
    }

    @Test
    void shouldReturnZeroWhenNoStaleItems() {
        when(deliveredOrderCompletionRepository.findStaleDeliveredItems(eq(20), any(Instant.class)))
                .thenReturn(List.of());

        AutoCompleteDeliveredOrdersResult result = useCase.execute();

        assertThat(result.candidatesFound()).isZero();
        verify(deliveredOrderCompletionRepository, never()).completeDeliveredItemsForOrder(any(), any(), any());
    }

    @Test
    void shouldGroupItemsByOrderAndComplete() {
        UUID orderId = UUID.randomUUID();
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();

        when(deliveredOrderCompletionRepository.findStaleDeliveredItems(eq(20), any(Instant.class)))
                .thenReturn(List.of(
                        new StaleDeliveredOrderItemCandidate(itemId1, orderId),
                        new StaleDeliveredOrderItemCandidate(itemId2, orderId)
                ));
        when(deliveredOrderCompletionRepository.completeDeliveredItemsForOrder(eq(orderId), any(), any(Instant.class)))
                .thenReturn(new DeliveredOrderCompletionResult(2, true, true, false));

        AutoCompleteDeliveredOrdersResult result = useCase.execute();

        assertThat(result.candidatesFound()).isEqualTo(2);
        assertThat(result.itemsCompleted()).isEqualTo(2);
        assertThat(result.ordersCompleted()).isEqualTo(1);
        assertThat(result.ordersProcessed()).isEqualTo(1);
        assertThat(result.failed()).isZero();
    }

    @Test
    void shouldCountFailedOrders() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(deliveredOrderCompletionRepository.findStaleDeliveredItems(eq(20), any(Instant.class)))
                .thenReturn(List.of(new StaleDeliveredOrderItemCandidate(itemId, orderId)));
        when(deliveredOrderCompletionRepository.completeDeliveredItemsForOrder(eq(orderId), any(), any(Instant.class)))
                .thenThrow(new RuntimeException("db error"));

        AutoCompleteDeliveredOrdersResult result = useCase.execute();

        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.itemsCompleted()).isZero();
    }
}
