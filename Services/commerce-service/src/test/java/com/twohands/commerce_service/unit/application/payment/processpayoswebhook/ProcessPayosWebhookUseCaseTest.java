package com.twohands.commerce_service.unit.application.payment.processpayoswebhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureUseCase;
import com.twohands.commerce_service.application.payment.processpayoswebhook.ProcessPayosWebhookResult;
import com.twohands.commerce_service.application.payment.processpayoswebhook.ProcessPayosWebhookUseCase;
import com.twohands.commerce_service.domain.payment.PaymentWebhookLogRepository;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessOutcome;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessRepository;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessResult;
import com.twohands.commerce_service.infrastructure.payos.PayosWebhookSignatureVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessPayosWebhookUseCaseTest {

    @Mock
    private PayosWebhookSignatureVerifier signatureVerifier;

    @Mock
    private PaymentWebhookLogRepository paymentWebhookLogRepository;

    @Mock
    private ProcessPayosPaymentSuccessRepository processPayosPaymentSuccessRepository;

    @Mock
    private HandlePaymentFailureUseCase handlePaymentFailureUseCase;

    private ProcessPayosWebhookUseCase useCase;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Instant now = Instant.parse("2026-06-04T15:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ProcessPayosWebhookUseCase(
                signatureVerifier,
                paymentWebhookLogRepository,
                processPayosPaymentSuccessRepository,
                handlePaymentFailureUseCase,
                objectMapper,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void successWebhookShouldMarkPaidAndEmitOutbox() {
        String payosOrderCode = "123456";
        UUID paymentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(signatureVerifier.verify(any())).thenReturn(true);
        when(paymentWebhookLogRepository.recordPayosWebhook(any(), eq(payosOrderCode), any(), eq(true)))
                .thenReturn(new PaymentWebhookLogRepository.WebhookLogRecord(true, false));
        when(processPayosPaymentSuccessRepository.markPaidByPayosOrderCode(
                eq(payosOrderCode),
                any(),
                any(),
                eq(now)
        )).thenReturn(ProcessPayosPaymentSuccessResult.processed(paymentId, orderId, now));

        ProcessPayosWebhookResult result = useCase.execute(successWebhook(payosOrderCode));

        assertThat(result.successWebhook()).isTrue();
        assertThat(result.successOutcome()).isEqualTo(ProcessPayosPaymentSuccessOutcome.PROCESSED);
        verify(processPayosPaymentSuccessRepository).markPaidByPayosOrderCode(
                eq(payosOrderCode),
                eq("PAYOS_WEBHOOK_PAID"),
                eq("SYSTEM:PAYOS_WEBHOOK"),
                eq(now)
        );
        verify(paymentWebhookLogRepository).markProcessed("PAYOS_00", payosOrderCode);
        verify(handlePaymentFailureUseCase, never()).execute(any());
    }

    @Test
    void successWebhookShouldBeIdempotentWhenAlreadyPaid() {
        String payosOrderCode = "999";
        when(signatureVerifier.verify(any())).thenReturn(true);
        when(paymentWebhookLogRepository.recordPayosWebhook(any(), eq(payosOrderCode), any(), eq(true)))
                .thenReturn(new PaymentWebhookLogRepository.WebhookLogRecord(true, false));
        when(processPayosPaymentSuccessRepository.markPaidByPayosOrderCode(
                eq(payosOrderCode),
                any(),
                any(),
                eq(now)
        )).thenReturn(ProcessPayosPaymentSuccessResult.skippedAlreadyPaid(UUID.randomUUID(), UUID.randomUUID()));

        ProcessPayosWebhookResult result = useCase.execute(successWebhook(payosOrderCode));

        assertThat(result.successOutcome()).isEqualTo(ProcessPayosPaymentSuccessOutcome.SKIPPED_ALREADY_PAID);
        verify(handlePaymentFailureUseCase, never()).execute(any());
    }

    private ObjectNode successWebhook(String payosOrderCode) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("code", "00");
        body.put("desc", "success");
        ObjectNode data = body.putObject("data");
        data.put("orderCode", payosOrderCode);
        return body;
    }
}
