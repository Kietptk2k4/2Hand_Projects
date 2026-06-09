package com.twohands.commerce_service.infrastructure.payos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.payment.PayosCheckoutUrlGateway;
import com.twohands.commerce_service.domain.payment.PayosCreateLinkCommand;
import com.twohands.commerce_service.domain.payment.PayosPaymentLinkResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PayosCheckoutUrlGatewayAdapter implements PayosCheckoutUrlGateway {

    private static final Logger log = LoggerFactory.getLogger(PayosCheckoutUrlGatewayAdapter.class);
    private static final String SUCCESS_CODE = "00";

    private final CommerceIntegrationProperties.Payos payosProperties;
    private final RestClient payosRestClient;
    private final PayosSignatureGenerator signatureGenerator;
    private final ObjectMapper objectMapper;

    public PayosCheckoutUrlGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            RestClient payosRestClient,
            PayosSignatureGenerator signatureGenerator,
            ObjectMapper objectMapper
    ) {
        this.payosProperties = integrationProperties.getPayos();
        this.payosRestClient = payosRestClient;
        this.signatureGenerator = signatureGenerator;
        this.objectMapper = objectMapper;
    }

    @Override
    public PayosPaymentLinkResult createPaymentLink(PayosCreateLinkCommand command) {
        if (payosProperties.isLiveClientConfigured()) {
            try {
                return createViaPayosApi(command);
            } catch (AppException ex) {
                if (!payosProperties.isMockFallbackEnabled()) {
                    throw ex;
                }
                log.warn("PayOS API failed for payment {}, using mock fallback: {}", command.paymentId(), ex.getMessage());
            } catch (RestClientException ex) {
                if (!payosProperties.isMockFallbackEnabled()) {
                    throw new AppException(ErrorCode.PAYOS_PROVIDER_UNAVAILABLE, "PayOS provider unavailable", ex);
                }
                log.warn("PayOS API unreachable for payment {}, using mock fallback", command.paymentId(), ex);
            }
        }

        if (payosProperties.isMockFallbackEnabled()) {
            return createMockLink(command);
        }

        throw new AppException(
                ErrorCode.PAYOS_PROVIDER_UNAVAILABLE,
                "PayOS integration is not configured"
        );
    }

    private PayosPaymentLinkResult createViaPayosApi(PayosCreateLinkCommand command) {
        String returnUrl = payosProperties.getReturnUrl();
        String cancelUrl = payosProperties.getCancelUrl();
        String signatureData = signatureGenerator.buildPaymentRequestData(
                command.amountVnd(),
                cancelUrl,
                command.description(),
                command.orderCode(),
                returnUrl
        );
        String signature = signatureGenerator.sign(payosProperties.getChecksumKey(), signatureData);
        log.info("DATA={}", signatureData);
        log.info("SIGNATURE={}", signature);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("orderCode", command.orderCode());
        requestBody.put("amount", command.amountVnd());
        requestBody.put("description", command.description());
        requestBody.put("returnUrl", returnUrl);
        requestBody.put("cancelUrl", cancelUrl);
        requestBody.put("signature", signature);
        if (command.linkExpiredAt() != null) {
            requestBody.put("expiredAt", command.linkExpiredAt().getEpochSecond());
        }

        String rawResponse = payosRestClient.post()
                .uri("/v2/payment-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AppException(ErrorCode.PAYOS_PROVIDER_UNAVAILABLE, "PayOS returned an empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String code = root.path("code").asText();
            if (!SUCCESS_CODE.equals(code)) {
                String desc = root.path("desc").asText("PayOS request failed");
                throw new AppException(ErrorCode.PAYOS_PROVIDER_UNAVAILABLE, desc);
            }
            JsonNode data = root.path("data");
            JsonNode checkoutUrlNode = data.path("checkoutUrl");
            if (checkoutUrlNode.isMissingNode() || checkoutUrlNode.isNull()) {
                throw new AppException(ErrorCode.PAYOS_PROVIDER_UNAVAILABLE, "PayOS response missing checkoutUrl");
            }
            String checkoutUrl = checkoutUrlNode.asText();
            if (checkoutUrl.isBlank()) {
                throw new AppException(ErrorCode.PAYOS_PROVIDER_UNAVAILABLE, "PayOS response missing checkoutUrl");
            }
            String orderCode = data.hasNonNull("orderCode")
                    ? data.get("orderCode").asText()
                    : String.valueOf(command.orderCode());
            return new PayosPaymentLinkResult(
                    orderCode,
                    checkoutUrl,
                    command.linkExpiredAt(),
                    rawResponse,
                    false
            );
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.PAYOS_PROVIDER_UNAVAILABLE, "Cannot parse PayOS response", ex);
        }
    }

    private PayosPaymentLinkResult createMockLink(PayosCreateLinkCommand command) {
        String orderCode = String.valueOf(command.orderCode());
        String checkoutUrl = "https://mock.payos.local/checkout/"
                + command.paymentId()
                + "?orderCode="
                + orderCode;

        Map<String, Object> mockPayload = new LinkedHashMap<>();
        mockPayload.put("provider", "MOCK");
        mockPayload.put("orderCode", command.orderCode());
        mockPayload.put("checkoutUrl", checkoutUrl);
        mockPayload.put("paymentId", command.paymentId().toString());
        mockPayload.put("orderId", command.orderId().toString());

        try {
            return new PayosPaymentLinkResult(
                    orderCode,
                    checkoutUrl,
                    command.linkExpiredAt(),
                    objectMapper.writeValueAsString(mockPayload),
                    true
            );
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize mock PayOS payload", ex);
        }
    }
}
