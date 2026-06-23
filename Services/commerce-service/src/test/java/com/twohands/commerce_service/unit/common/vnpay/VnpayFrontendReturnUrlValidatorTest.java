package com.twohands.commerce_service.unit.common.vnpay;

import com.twohands.commerce_service.common.vnpay.VnpayFrontendReturnUrlValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VnpayFrontendReturnUrlValidatorTest {

  @Mock
  private HttpServletRequest request;

  private VnpayFrontendReturnUrlValidator validator;

  @BeforeEach
  void setUp() {
    validator = new VnpayFrontendReturnUrlValidator(
        "http://localhost:5173,http://127.0.0.1:5173",
        "twohands,exp"
    );
  }

  @Test
  void shouldAllowTwohandsDeepLink() {
    assertThat(validator.isAllowedFrontendReturnUrl("twohands://commerce/checkout/vnpay-return"))
        .isTrue();
  }

  @Test
  void shouldAllowExpoDevDeepLink() {
    assertThat(validator.isAllowedFrontendReturnUrl(
        "exp://192.168.1.4:8081/--/commerce/checkout/vnpay-return"
    )).isTrue();
  }

  @Test
  void shouldAllowWebFrontendReturn() {
    assertThat(validator.isAllowedFrontendReturnUrl(
        "http://localhost:5173/commerce/checkout/vnpay-return"
    )).isTrue();
  }

  @Test
  void shouldAllowBackendReturnFromSameRequestHost() {
    when(request.getHeader("X-Forwarded-Proto")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("192.168.1.4");
    when(request.getServerPort()).thenReturn(3003);

    assertThat(validator.isAllowedBackendReturnUrl(
        "http://192.168.1.4:3003/commerce/api/v1/payments/vnpay/return",
        request
    )).isTrue();
  }

  @Test
  void shouldRejectUnknownFrontendReturn() {
    assertThat(validator.isAllowedFrontendReturnUrl("https://evil.example/steal"))
        .isFalse();
  }
}
