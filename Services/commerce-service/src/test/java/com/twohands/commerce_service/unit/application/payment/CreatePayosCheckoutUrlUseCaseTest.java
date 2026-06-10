package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.createpayoscheckouturl.CreatePayosCheckoutUrlCommand;
import com.twohands.commerce_service.application.payment.createpayoscheckouturl.CreatePayosCheckoutUrlUseCase;
import com.twohands.commerce_service.config.CommerceCheckoutProperties;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlRepository;
import com.twohands.commerce_service.domain.payment.CreatePayosCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentPayosSnapshot;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PayosCheckoutUrlGateway;
import com.twohands.commerce_service.domain.payment.PayosPaymentLinkResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePayosCheckoutUrlUseCaseTest {

    @Mock
    private CreatePayosCheckoutUrlRepository createPayosCheckoutUrlRepository;

    @Mock
    private PayosCheckoutUrlGateway payosCheckoutUrlGateway;

    @Mock
    private CommerceCheckoutProperties checkoutProperties;

    private CreatePayosCheckoutUrlUseCase useCase;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    private CreatePayosCheckoutUrlUseCase buildUseCase(boolean codOnlyEnabled) {
        lenient().when(checkoutProperties.isCodOnlyEnabled()).thenReturn(codOnlyEnabled);
        return new CreatePayosCheckoutUrlUseCase(
                createPayosCheckoutUrlRepository,
                payosCheckoutUrlGateway,
                clock,
                checkoutProperties
        );
    }

    @Test
    void shouldReuseExistingNonExpiredCheckoutUrl() {
        useCase = buildUseCase(false);
        when(createPayosCheckoutUrlRepository.findPaymentForBuyer(paymentId, buyerId))
                .thenReturn(Optional.of(pendingSnapshot(
                        "12345",
                        "https://pay.payos.vn/web/existing",
                        now.plusSeconds(600)
                )));

        CreatePayosCheckoutUrlResult result = useCase.execute(new CreatePayosCheckoutUrlCommand(paymentId, buyerId));

        assertThat(result.reusedExistingUrl()).isTrue();
        assertThat(result.payosCheckoutUrl()).isEqualTo("https://pay.payos.vn/web/existing");
        verify(payosCheckoutUrlGateway, never()).createPaymentLink(any());
        verify(createPayosCheckoutUrlRepository, never()).savePayosCheckoutFields(any(), any(), any(), any());
    }

    @Test
    void shouldCreateNewCheckoutUrlWhenExistingExpired() {
        useCase = buildUseCase(false);
        when(createPayosCheckoutUrlRepository.findPaymentForBuyer(paymentId, buyerId))
                .thenReturn(Optional.of(pendingSnapshot(
                        "12345",
                        "https://pay.payos.vn/web/old",
                        now.minusSeconds(60)
                )));
        PayosPaymentLinkResult providerResult = new PayosPaymentLinkResult(
                "99999",
                "https://pay.payos.vn/web/new",
                now.plusSeconds(1800),
                "{\"code\":\"00\"}",
                true
        );
        when(payosCheckoutUrlGateway.createPaymentLink(any())).thenReturn(providerResult);
        when(createPayosCheckoutUrlRepository.savePayosCheckoutFields(eq(paymentId), eq(orderId), eq(providerResult), eq(now)))
                .thenReturn(new CreatePayosCheckoutUrlResult(
                        paymentId,
                        orderId,
                        "99999",
                        "https://pay.payos.vn/web/new",
                        now.plusSeconds(1800),
                        false
                ));

        CreatePayosCheckoutUrlResult result = useCase.execute(new CreatePayosCheckoutUrlCommand(paymentId, buyerId));

        assertThat(result.reusedExistingUrl()).isFalse();
        assertThat(result.payosCheckoutUrl()).isEqualTo("https://pay.payos.vn/web/new");
        verify(payosCheckoutUrlGateway).createPaymentLink(any());
    }

    @Test
    void shouldRejectWhenPaymentNotFound() {
        useCase = buildUseCase(false);
        when(createPayosCheckoutUrlRepository.findPaymentForBuyer(paymentId, buyerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CreatePayosCheckoutUrlCommand(paymentId, buyerId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenPaymentNotPending() {
        useCase = buildUseCase(false);
        PaymentPayosSnapshot paid = new PaymentPayosSnapshot(
                paymentId,
                orderId,
                buyerId,
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.PAYOS,
                PaymentStatus.PAID,
                OrderStatus.AWAITING_PAYMENT,
                null,
                null,
                null,
                now.plusSeconds(600)
        );
        when(createPayosCheckoutUrlRepository.findPaymentForBuyer(paymentId, buyerId))
                .thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> useCase.execute(new CreatePayosCheckoutUrlCommand(paymentId, buyerId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_STATE);
    }

    @Test
    void shouldRejectWhenCodOnlyEnabled() {
        useCase = buildUseCase(true);
        when(createPayosCheckoutUrlRepository.findPaymentForBuyer(paymentId, buyerId))
                .thenReturn(Optional.of(pendingSnapshot(
                        "12345",
                        "https://pay.payos.vn/web/existing",
                        now.plusSeconds(600)
                )));

        assertThatThrownBy(() -> useCase.execute(new CreatePayosCheckoutUrlCommand(paymentId, buyerId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PAYMENT_METHOD);
    }

    @Test
    void shouldRejectWhenOrderNotAwaitingPayment() {
        useCase = buildUseCase(false);
        PaymentPayosSnapshot processing = new PaymentPayosSnapshot(
                paymentId,
                orderId,
                buyerId,
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.PAYOS,
                PaymentStatus.PENDING,
                OrderStatus.PROCESSING,
                null,
                null,
                null,
                now.plusSeconds(600)
        );
        when(createPayosCheckoutUrlRepository.findPaymentForBuyer(paymentId, buyerId))
                .thenReturn(Optional.of(processing));

        assertThatThrownBy(() -> useCase.execute(new CreatePayosCheckoutUrlCommand(paymentId, buyerId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ORDER_NOT_AWAITING_PAYMENT);
    }

    private PaymentPayosSnapshot pendingSnapshot(String orderCode, String checkoutUrl, Instant checkoutExpiredAt) {
        return new PaymentPayosSnapshot(
                paymentId,
                orderId,
                buyerId,
                BigDecimal.valueOf(1_000_000),
                PaymentMethod.PAYOS,
                PaymentStatus.PENDING,
                OrderStatus.AWAITING_PAYMENT,
                orderCode,
                checkoutUrl,
                checkoutExpiredAt,
                now.plusSeconds(1800)
        );
    }
}
