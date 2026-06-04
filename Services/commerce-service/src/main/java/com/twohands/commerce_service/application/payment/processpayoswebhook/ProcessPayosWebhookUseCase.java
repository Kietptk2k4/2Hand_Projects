package com.twohands.commerce_service.application.payment.processpayoswebhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureCommand;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureUseCase;
import com.twohands.commerce_service.domain.payment.HandlePaymentFailureResult;
import com.twohands.commerce_service.domain.payment.PaymentFailureOutcome;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentWebhookLogRepository;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessRepository;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessResult;
import com.twohands.commerce_service.infrastructure.payos.PayosWebhookSignatureVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class ProcessPayosWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPayosWebhookUseCase.class);
    private static final String CHANGED_BY = "SYSTEM:PAYOS_WEBHOOK";
    private static final String SUCCESS_CODE = "00";

    private final PayosWebhookSignatureVerifier signatureVerifier;
    private final PaymentWebhookLogRepository paymentWebhookLogRepository;
    private final ProcessPayosPaymentSuccessRepository processPayosPaymentSuccessRepository;
    private final HandlePaymentFailureUseCase handlePaymentFailureUseCase;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ProcessPayosWebhookUseCase(
            PayosWebhookSignatureVerifier signatureVerifier,
            PaymentWebhookLogRepository paymentWebhookLogRepository,
            ProcessPayosPaymentSuccessRepository processPayosPaymentSuccessRepository,
            HandlePaymentFailureUseCase handlePaymentFailureUseCase,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.signatureVerifier = signatureVerifier;
        this.paymentWebhookLogRepository = paymentWebhookLogRepository;
        this.processPayosPaymentSuccessRepository = processPayosPaymentSuccessRepository;
        this.handlePaymentFailureUseCase = handlePaymentFailureUseCase;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public ProcessPayosWebhookResult execute(JsonNode webhookBody) {
        String payloadJson = serialize(webhookBody);
        boolean signatureValid = signatureVerifier.verify(webhookBody);
        String payosOrderCode = extractPayosOrderCode(webhookBody);
        String eventType = resolveEventType(webhookBody);

        PaymentWebhookLogRepository.WebhookLogRecord logRecord = paymentWebhookLogRepository.recordPayosWebhook(
                eventType,
                payosOrderCode,
                payloadJson,
                signatureValid
        );

        if (logRecord.alreadyProcessed()) {
            return ProcessPayosWebhookResult.duplicate(eventType, payosOrderCode, signatureValid);
        }

        if (!signatureValid) {
            log.warn("Rejected PayOS webhook with invalid signature for orderCode={}", payosOrderCode);
            return ProcessPayosWebhookResult.invalidSignature(eventType, payosOrderCode);
        }

        if (SUCCESS_CODE.equals(webhookBody.path("code").asText())) {
            ProcessPayosPaymentSuccessResult successResult = processPayosPaymentSuccessRepository.markPaidByPayosOrderCode(
                    payosOrderCode,
                    "PAYOS_WEBHOOK_PAID",
                    CHANGED_BY,
                    clock.instant()
            );
            paymentWebhookLogRepository.markProcessed(eventType, payosOrderCode);
            log.info(
                    "PayOS success webhook outcome={} orderCode={} paymentId={}",
                    successResult.outcome(),
                    payosOrderCode,
                    successResult.paymentId()
            );
            return ProcessPayosWebhookResult.processedSuccess(eventType, payosOrderCode, successResult.outcome());
        }

        PaymentStatus terminalStatus = mapFailureStatus(webhookBody);
        HandlePaymentFailureResult failureResult = handlePaymentFailureUseCase.execute(
                HandlePaymentFailureCommand.byPayosOrderCode(
                        payosOrderCode,
                        terminalStatus,
                        "PAYOS_WEBHOOK_" + terminalStatus.name(),
                        CHANGED_BY,
                        payloadJson
                )
        );

        paymentWebhookLogRepository.markProcessed(eventType, payosOrderCode);

        if (failureResult.outcome() == PaymentFailureOutcome.SKIPPED_ALREADY_PAID) {
            log.warn("PayOS failure webhook ignored because payment already PAID (orderCode={})", payosOrderCode);
        }

        return ProcessPayosWebhookResult.processedFailure(
                eventType,
                payosOrderCode,
                terminalStatus,
                failureResult.outcome()
        );
    }

    private PaymentStatus mapFailureStatus(JsonNode webhookBody) {
        String desc = webhookBody.path("desc").asText("").toLowerCase();
        if (desc.contains("cancel")) {
            return PaymentStatus.CANCELLED;
        }
        if (desc.contains("expire")) {
            return PaymentStatus.EXPIRED;
        }
        return PaymentStatus.FAILED;
    }

    private String resolveEventType(JsonNode webhookBody) {
        String code = webhookBody.path("code").asText("UNKNOWN");
        return "PAYOS_" + code;
    }

    private String extractPayosOrderCode(JsonNode webhookBody) {
        JsonNode orderCode = webhookBody.path("data").path("orderCode");
        if (orderCode.isMissingNode() || orderCode.isNull()) {
            return "UNKNOWN";
        }
        return orderCode.asText();
    }

    private String serialize(JsonNode webhookBody) {
        try {
            return objectMapper.writeValueAsString(webhookBody);
        } catch (JsonProcessingException ex) {
            return webhookBody.toString();
        }
    }
}
