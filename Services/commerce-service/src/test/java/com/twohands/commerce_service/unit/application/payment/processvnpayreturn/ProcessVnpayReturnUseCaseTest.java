package com.twohands.commerce_service.unit.application.payment.processvnpayreturn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureUseCase;
import com.twohands.commerce_service.application.payment.processvnpayreturn.ProcessVnpayReturnResult;
import com.twohands.commerce_service.application.payment.processvnpayreturn.ProcessVnpayReturnUseCase;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessOutcome;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessResult;
import com.twohands.commerce_service.domain.payment.ProcessVnpayPaymentSuccessRepository;
import com.twohands.commerce_service.domain.payment.VnpayTxnRefParser;
import com.twohands.commerce_service.infrastructure.vnpay.VnpayParamSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessVnpayReturnUseCaseTest {

  private static final String HASH_SECRET = "TEST_VNPAY_SECRET";

  @Mock
  private ProcessVnpayPaymentSuccessRepository processVnpayPaymentSuccessRepository;

  @Mock
  private HandlePaymentFailureUseCase handlePaymentFailureUseCase;

  private ProcessVnpayReturnUseCase useCase;
  private final UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
  private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

  @BeforeEach
  void setUp() {
    CommerceIntegrationProperties properties = new CommerceIntegrationProperties();
    CommerceIntegrationProperties.Vnpay vnpay = properties.getVnpay();
    vnpay.setHashSecret(HASH_SECRET);
    vnpay.setFrontendReturnBaseUrl("http://localhost:5173");

    useCase = new ProcessVnpayReturnUseCase(
        properties,
        new VnpayParamSigner(),
        processVnpayPaymentSuccessRepository,
        handlePaymentFailureUseCase,
        new ObjectMapper(),
        Clock.fixed(now, ZoneOffset.UTC)
    );
  }

  @Test
  void shouldRedirectSuccessWhenSignatureValid() {
    String txnRef = VnpayTxnRefParser.buildTxnRef(orderId, now);
    Map<String, String> params = signedReturnParams(txnRef, "00");

    when(processVnpayPaymentSuccessRepository.markPaidByVnpayTxnRef(any(), any(), any(), any(), any()))
        .thenReturn(ProcessPayosPaymentSuccessResult.processed(UUID.randomUUID(), orderId, now));

    ProcessVnpayReturnResult result = useCase.execute(params);

    assertThat(result.success()).isTrue();
    assertThat(result.redirectUri().toString()).contains("status=success");
    verify(processVnpayPaymentSuccessRepository).markPaidByVnpayTxnRef(any(), any(), any(), any(), any());
    verify(handlePaymentFailureUseCase, never()).execute(any());
  }

  @Test
  void shouldRedirectFailureWhenResponseCodeNotSuccess() {
    String txnRef = VnpayTxnRefParser.buildTxnRef(orderId, now);
    Map<String, String> params = signedReturnParams(txnRef, "24");

    ProcessVnpayReturnResult result = useCase.execute(params);

    assertThat(result.success()).isFalse();
    assertThat(result.redirectUri().toString()).contains("status=failed");
    verify(handlePaymentFailureUseCase).execute(any());
  }

  private Map<String, String> signedReturnParams(String txnRef, String responseCode) {
    SortedMap<String, String> signParams = new TreeMap<>();
    signParams.put("vnp_ResponseCode", responseCode);
    signParams.put("vnp_TxnRef", txnRef);
    signParams.put("vnp_Amount", "100000000");

    String secureHash = new VnpayParamSigner().sign(signParams, HASH_SECRET);

    Map<String, String> params = new LinkedHashMap<>(signParams);
    params.put("vnp_SecureHash", secureHash);
    return params;
  }
}
