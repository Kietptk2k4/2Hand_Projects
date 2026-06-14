package com.twohands.commerce_service.unit.application.payment;

import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlCommand;
import com.twohands.commerce_service.application.payment.createvnpaycheckouturl.CreateVnpayCheckoutUrlUseCase;
import com.twohands.commerce_service.config.CommerceCheckoutProperties;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlRepository;
import com.twohands.commerce_service.domain.payment.CreateVnpayCheckoutUrlResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentVnpaySnapshot;
import com.twohands.commerce_service.domain.payment.VnpayCheckoutUrlGateway;
import com.twohands.commerce_service.domain.payment.VnpayPaymentUrlResult;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateVnpayCheckoutUrlUseCaseTest {

  @Mock
  private CreateVnpayCheckoutUrlRepository repository;

  @Mock
  private VnpayCheckoutUrlGateway gateway;

  @Mock
  private CommerceCheckoutProperties checkoutProperties;

  private final UUID paymentId = UUID.randomUUID();
  private final UUID orderId = UUID.randomUUID();
  private final UUID buyerId = UUID.randomUUID();
  private final Instant now = Instant.parse("2026-05-21T10:00:00Z");
  private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

  @Test
  void shouldCreateVnpayCheckoutUrl() {
    when(checkoutProperties.isCodOnlyEnabled()).thenReturn(false);
    when(repository.findPaymentForBuyer(paymentId, buyerId)).thenReturn(Optional.of(snapshot()));
    when(gateway.createPaymentUrl(any())).thenReturn(new VnpayPaymentUrlResult(
        "txn-ref",
        "https://sandbox.vnpayment.vn/pay",
        "{}",
        true
    ));
    when(repository.saveVnpayCheckoutFields(any(), any(), any(), any())).thenReturn(
        new CreateVnpayCheckoutUrlResult(paymentId, orderId, "txn-ref", "https://sandbox.vnpayment.vn/pay")
    );

    CreateVnpayCheckoutUrlUseCase useCase = new CreateVnpayCheckoutUrlUseCase(repository, gateway, clock, checkoutProperties);
    CreateVnpayCheckoutUrlResult result = useCase.execute(new CreateVnpayCheckoutUrlCommand(paymentId, buyerId, "127.0.0.1"));

    assertThat(result.checkoutUrl()).contains("vnpayment");
    verify(repository).saveVnpayCheckoutFields(any(), any(), any(), any());
  }

  @Test
  void shouldRejectWhenCodOnlyEnabled() {
    when(checkoutProperties.isCodOnlyEnabled()).thenReturn(true);
    when(repository.findPaymentForBuyer(paymentId, buyerId)).thenReturn(Optional.of(snapshot()));

    CreateVnpayCheckoutUrlUseCase useCase = new CreateVnpayCheckoutUrlUseCase(repository, gateway, clock, checkoutProperties);

    assertThatThrownBy(() -> useCase.execute(new CreateVnpayCheckoutUrlCommand(paymentId, buyerId, "127.0.0.1")))
        .isInstanceOf(AppException.class)
        .extracting(ex -> ((AppException) ex).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_PAYMENT_METHOD);
  }

  private PaymentVnpaySnapshot snapshot() {
    return new PaymentVnpaySnapshot(
        paymentId,
        orderId,
        buyerId,
        BigDecimal.valueOf(1_000_000),
        PaymentMethod.VNPAY,
        PaymentStatus.PENDING,
        OrderStatus.AWAITING_PAYMENT,
        null,
        now.plusSeconds(1800)
    );
  }
}
