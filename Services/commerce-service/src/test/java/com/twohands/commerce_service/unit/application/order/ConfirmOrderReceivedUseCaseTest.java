package com.twohands.commerce_service.unit.application.order;

import com.twohands.commerce_service.application.order.confirmorderreceived.ConfirmOrderReceivedCommand;
import com.twohands.commerce_service.application.order.confirmorderreceived.ConfirmOrderReceivedUseCase;
import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedRepository;
import com.twohands.commerce_service.domain.order.ConfirmOrderReceivedResult;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderReceivedUseCaseTest {

    @Mock
    private ConfirmOrderReceivedRepository confirmOrderReceivedRepository;

    @InjectMocks
    private ConfirmOrderReceivedUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @Test
    void shouldConfirmReceivedAndCompleteOrder() {
        when(confirmOrderReceivedRepository.confirmReceivedByBuyer(eq(buyerId), eq(orderId), any()))
                .thenReturn(new ConfirmOrderReceivedResult(
                        orderId,
                        OrderStatus.COMPLETED,
                        PaymentStatus.PAID,
                        2,
                        true,
                        true,
                        false
                ));

        ConfirmOrderReceivedResult result = useCase.execute(new ConfirmOrderReceivedCommand(buyerId, orderId));

        assertThat(result.itemsCompleted()).isEqualTo(2);
        assertThat(result.orderCompleted()).isTrue();
        assertThat(result.paymentMarkedPaid()).isTrue();
        assertThat(useCase.successMessage(false)).isEqualTo("Xac nhan da nhan hang thanh cong.");
    }

    @Test
    void shouldReturnIdempotentMessageWhenAlreadyCompleted() {
        when(confirmOrderReceivedRepository.confirmReceivedByBuyer(any(), any(), any()))
                .thenReturn(new ConfirmOrderReceivedResult(
                        orderId,
                        OrderStatus.COMPLETED,
                        PaymentStatus.PAID,
                        0,
                        false,
                        false,
                        true
                ));

        ConfirmOrderReceivedResult result = useCase.execute(new ConfirmOrderReceivedCommand(buyerId, orderId));

        assertThat(result.alreadyCompleted()).isTrue();
        assertThat(useCase.successMessage(true)).contains("truoc do");
    }
}
