package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.viewpaymentstatus.ViewPaymentStatusCommand;
import com.twohands.commerce_service.application.payment.viewpaymentstatus.ViewPaymentStatusUseCase;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusRepository;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusResult;
import com.twohands.commerce_service.domain.payment.ViewPaymentStatusSnapshot;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewPaymentStatusUseCaseTest {

    @Mock
    private ViewPaymentStatusRepository viewPaymentStatusRepository;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    private ViewPaymentStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ViewPaymentStatusUseCase(viewPaymentStatusRepository, clock);
    }

    @Test
    void shouldReturnPaymentStatusWithReusablePayosCheckoutUrl() {
        when(viewPaymentStatusRepository.findByPaymentIdAndBuyerId(paymentId, buyerId))
                .thenReturn(Optional.of(snapshot(
                        PaymentStatus.PENDING,
                        PaymentMethod.PAYOS,
                        "https://pay.payos.vn/web/checkout",
                        now.plusSeconds(600),
                        OrderStatus.AWAITING_PAYMENT,
                        PaymentStatus.PENDING
                )));

        ViewPaymentStatusResult result = useCase.execute(new ViewPaymentStatusCommand(buyerId, paymentId));

        assertThat(result.paymentId()).isEqualTo(paymentId);
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.payosCheckoutUrl()).isEqualTo("https://pay.payos.vn/web/checkout");
        assertThat(result.orderStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(result.orderPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldNotReturnCheckoutUrlWhenExpired() {
        when(viewPaymentStatusRepository.findByPaymentIdAndBuyerId(paymentId, buyerId))
                .thenReturn(Optional.of(snapshot(
                        PaymentStatus.PENDING,
                        PaymentMethod.PAYOS,
                        "https://pay.payos.vn/web/checkout",
                        now.minusSeconds(1),
                        OrderStatus.AWAITING_PAYMENT,
                        PaymentStatus.PENDING
                )));

        ViewPaymentStatusResult result = useCase.execute(new ViewPaymentStatusCommand(buyerId, paymentId));

        assertThat(result.payosCheckoutUrl()).isNull();
    }

    @Test
    void shouldNotReturnCheckoutUrlWhenPaymentPaid() {
        when(viewPaymentStatusRepository.findByPaymentIdAndBuyerId(paymentId, buyerId))
                .thenReturn(Optional.of(snapshot(
                        PaymentStatus.PAID,
                        PaymentMethod.PAYOS,
                        "https://pay.payos.vn/web/checkout",
                        now.plusSeconds(600),
                        OrderStatus.PROCESSING,
                        PaymentStatus.PAID
                )));

        ViewPaymentStatusResult result = useCase.execute(new ViewPaymentStatusCommand(buyerId, paymentId));

        assertThat(result.payosCheckoutUrl()).isNull();
        assertThat(result.paidAt()).isEqualTo(now);
    }

    @Test
    void shouldNotReturnCheckoutUrlForNonPayosMethod() {
        when(viewPaymentStatusRepository.findByPaymentIdAndBuyerId(paymentId, buyerId))
                .thenReturn(Optional.of(snapshot(
                        PaymentStatus.PENDING,
                        PaymentMethod.COD,
                        null,
                        null,
                        OrderStatus.AWAITING_PAYMENT,
                        PaymentStatus.PENDING
                )));

        ViewPaymentStatusResult result = useCase.execute(new ViewPaymentStatusCommand(buyerId, paymentId));

        assertThat(result.payosCheckoutUrl()).isNull();
    }

    @Test
    void shouldThrowWhenPaymentNotFoundOrNotOwned() {
        when(viewPaymentStatusRepository.findByPaymentIdAndBuyerId(paymentId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewPaymentStatusCommand(buyerId, paymentId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }

    private ViewPaymentStatusSnapshot snapshot(
            PaymentStatus status,
            PaymentMethod method,
            String payosCheckoutUrl,
            Instant checkoutUrlExpiredAt,
            OrderStatus orderStatus,
            PaymentStatus orderPaymentStatus
    ) {
        return new ViewPaymentStatusSnapshot(
                paymentId,
                orderId,
                method,
                BigDecimal.valueOf(1_050_000),
                "VND",
                status,
                status == PaymentStatus.PAID ? now : null,
                null,
                payosCheckoutUrl,
                checkoutUrlExpiredAt,
                orderStatus,
                orderPaymentStatus
        );
    }
}
