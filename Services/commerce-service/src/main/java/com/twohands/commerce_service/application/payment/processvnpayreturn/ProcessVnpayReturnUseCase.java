package com.twohands.commerce_service.application.payment.processvnpayreturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureCommand;
import com.twohands.commerce_service.application.payment.handlepaymentfailure.HandlePaymentFailureUseCase;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessResult;
import com.twohands.commerce_service.domain.payment.ProcessVnpayPaymentSuccessRepository;
import com.twohands.commerce_service.domain.payment.VnpayTxnRefParser;
import com.twohands.commerce_service.infrastructure.vnpay.VnpayParamSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProcessVnpayReturnUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessVnpayReturnUseCase.class);
    private static final String CHANGED_BY = "SYSTEM:VNPAY_RETURN";
    private static final String SUCCESS_RESPONSE_CODE = "00";

    private final CommerceIntegrationProperties.Vnpay vnpayProperties;
    private final VnpayParamSigner paramSigner;
    private final ProcessVnpayPaymentSuccessRepository processVnpayPaymentSuccessRepository;
    private final HandlePaymentFailureUseCase handlePaymentFailureUseCase;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ProcessVnpayReturnUseCase(
            CommerceIntegrationProperties integrationProperties,
            VnpayParamSigner paramSigner,
            ProcessVnpayPaymentSuccessRepository processVnpayPaymentSuccessRepository,
            HandlePaymentFailureUseCase handlePaymentFailureUseCase,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.vnpayProperties = integrationProperties.getVnpay();
        this.paramSigner = paramSigner;
        this.processVnpayPaymentSuccessRepository = processVnpayPaymentSuccessRepository;
        this.handlePaymentFailureUseCase = handlePaymentFailureUseCase;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public ProcessVnpayReturnResult execute(Map<String, String> queryParams) {
        try {
            String txnRef = queryParams.get("vnp_TxnRef");
            UUID orderId = VnpayTxnRefParser.parseOrderId(txnRef);
            String responseCode = queryParams.get("vnp_ResponseCode");
            String secureHash = queryParams.get("vnp_SecureHash");
            String transactionNo = queryParams.get("vnp_TransactionNo");

            boolean signatureValid = StringUtils.hasText(vnpayProperties.getHashSecret())
                    && paramSigner.verify(queryParams, secureHash, vnpayProperties.getHashSecret());
            boolean success = signatureValid && SUCCESS_RESPONSE_CODE.equals(responseCode);

            if (success) {
                ProcessPayosPaymentSuccessResult paidResult = processVnpayPaymentSuccessRepository.markPaidByVnpayTxnRef(
                        txnRef,
                        transactionNo,
                        "VNPAY_RETURN_PAID",
                        CHANGED_BY,
                        clock.instant()
                );
                log.info(
                        "VNPay return success outcome={} txnRef={} paymentId={}",
                        paidResult.outcome(),
                        txnRef,
                        paidResult.paymentId()
                );
                return ProcessVnpayReturnResult.success(
                        buildFrontendRedirect("success", orderId),
                        orderId,
                        txnRef
                );
            }

            if (StringUtils.hasText(txnRef)) {
                handlePaymentFailureUseCase.execute(HandlePaymentFailureCommand.byVnpayTxnRef(
                        txnRef,
                        PaymentStatus.FAILED,
                        "VNPAY_RETURN_FAILED",
                        CHANGED_BY,
                        serializeReturnPayload(queryParams, signatureValid, responseCode)
                ));
            }

            return ProcessVnpayReturnResult.failure(
                    buildFrontendRedirect("failed", orderId),
                    orderId,
                    txnRef
            );
        } catch (Exception ex) {
            log.error("VNPay return processing failed", ex);
            return ProcessVnpayReturnResult.unknownError(buildOrdersErrorRedirect());
        }
    }

    private URI buildFrontendRedirect(String status, UUID orderId) {
        String base = trimTrailingSlash(vnpayProperties.getFrontendReturnBaseUrl());
        StringBuilder url = new StringBuilder(base)
                .append("/checkout/vnpay-return?status=")
                .append(encode(status));
        if (orderId != null) {
            url.append("&orderId=").append(encode(orderId.toString()));
        } else {
            url.append("&orderId=unknown");
        }
        return URI.create(url.toString());
    }

    private URI buildOrdersErrorRedirect() {
        String base = trimTrailingSlash(vnpayProperties.getFrontendReturnBaseUrl());
        return URI.create(base + "/orders?error=unknown");
    }

    private String serializeReturnPayload(Map<String, String> queryParams, boolean signatureValid, String responseCode) {
        Map<String, Object> payload = new LinkedHashMap<>(queryParams);
        payload.put("signature_valid", signatureValid);
        payload.put("response_code", responseCode);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return queryParams.toString();
        }
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:5173";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
