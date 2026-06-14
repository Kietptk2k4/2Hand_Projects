package com.twohands.commerce_service.unit.infrastructure.vnpay;

import com.twohands.commerce_service.infrastructure.vnpay.VnpayParamSigner;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class VnpayParamSignerTest {

  private final VnpayParamSigner signer = new VnpayParamSigner();

  @Test
  void shouldVerifyMatchingSignature() {
    SortedMap<String, String> params = new TreeMap<>();
    params.put("vnp_Amount", "2253000000");
    params.put("vnp_Command", "pay");
    params.put("vnp_TxnRef", "550e8400-e29b-41d4-a716-446655440000-1710000000000");

    String hashSecret = "TEST_SECRET";
    String secureHash = signer.sign(params, hashSecret);

    Map<String, String> returnParams = new LinkedHashMap<>(params);
    returnParams.put("vnp_SecureHash", secureHash);

    assertThat(signer.verify(returnParams, secureHash, hashSecret)).isTrue();
  }

  @Test
  void shouldRejectTamperedSignature() {
    SortedMap<String, String> params = new TreeMap<>();
    params.put("vnp_Amount", "1000000");
    params.put("vnp_TxnRef", "ref-1");

    String secureHash = signer.sign(params, "SECRET");

    Map<String, String> returnParams = new LinkedHashMap<>(params);
    returnParams.put("vnp_SecureHash", secureHash);
    returnParams.put("vnp_Amount", "2000000");

    assertThat(signer.verify(returnParams, secureHash, "SECRET")).isFalse();
  }
}
