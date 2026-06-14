package com.twohands.commerce_service.unit.domain.payment;

import com.twohands.commerce_service.domain.payment.VnpayTxnRefParser;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VnpayTxnRefParserTest {

  private final UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

  @Test
  void shouldBuildAndParseTxnRef() {
    Instant now = Instant.parse("2026-05-21T10:00:00Z");
    String txnRef = VnpayTxnRefParser.buildTxnRef(orderId, now);

    assertThat(VnpayTxnRefParser.parseOrderId(txnRef)).isEqualTo(orderId);
  }

  @Test
  void shouldReturnNullForInvalidTxnRef() {
    assertThat(VnpayTxnRefParser.parseOrderId("42-1710000000000")).isNull();
    assertThat(VnpayTxnRefParser.parseOrderId(null)).isNull();
  }
}
