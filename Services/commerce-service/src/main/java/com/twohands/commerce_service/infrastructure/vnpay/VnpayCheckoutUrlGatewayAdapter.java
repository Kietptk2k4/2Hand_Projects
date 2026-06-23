package com.twohands.commerce_service.infrastructure.vnpay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.domain.payment.VnpayCheckoutUrlGateway;
import com.twohands.commerce_service.domain.payment.VnpayCreatePaymentUrlCommand;
import com.twohands.commerce_service.domain.payment.VnpayPaymentUrlResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class VnpayCheckoutUrlGatewayAdapter implements VnpayCheckoutUrlGateway {

    private static final Logger log = LoggerFactory.getLogger(VnpayCheckoutUrlGatewayAdapter.class);
    private static final ZoneId VNPAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter CREATE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final CommerceIntegrationProperties.Vnpay vnpayProperties;
    private final VnpayParamSigner paramSigner;
    private final ObjectMapper objectMapper;

    public VnpayCheckoutUrlGatewayAdapter(
            CommerceIntegrationProperties integrationProperties,
            VnpayParamSigner paramSigner,
            ObjectMapper objectMapper
    ) {
        this.vnpayProperties = integrationProperties.getVnpay();
        this.paramSigner = paramSigner;
        this.objectMapper = objectMapper;
    }

    @Override
    public VnpayPaymentUrlResult createPaymentUrl(VnpayCreatePaymentUrlCommand command) {
        if (vnpayProperties.isLiveClientConfigured()) {
            try {
                return createLiveUrl(command);
            } catch (AppException ex) {
                if (!vnpayProperties.isMockFallbackEnabled()) {
                    throw ex;
                }
                log.warn("VNPay URL build failed for payment {}, using mock fallback: {}",
                        command.paymentId(), ex.getMessage());
            }
        }

        if (vnpayProperties.isMockFallbackEnabled()) {
            return createMockUrl(command);
        }

        throw new AppException(
                ErrorCode.VNPAY_PROVIDER_UNAVAILABLE,
                "VNPay integration is not configured"
        );
    }

    private VnpayPaymentUrlResult createLiveUrl(VnpayCreatePaymentUrlCommand command) {
        SortedMap<String, String> params = buildBaseParams(command);
        String secureHash = paramSigner.sign(params, vnpayProperties.getHashSecret());
        params.put("vnp_SecureHash", secureHash);

        String query = paramSigner.buildQueryString(params);
        String paymentUrl = trimTrailingSlash(vnpayProperties.getPayUrl()) + "?" + query;
        return new VnpayPaymentUrlResult(
                command.txnRef(),
                paymentUrl,
                toJson(params),
                false
        );
    }

    private VnpayPaymentUrlResult createMockUrl(VnpayCreatePaymentUrlCommand command) {
        String paymentUrl = "https://mock.vnpay.local/checkout/"
                + command.paymentId()
                + "?txnRef="
                + command.txnRef();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("provider", "MOCK");
        payload.put("txnRef", command.txnRef());
        payload.put("paymentId", command.paymentId().toString());
        payload.put("orderId", command.orderId().toString());
        payload.put("checkoutUrl", paymentUrl);

        try {
            return new VnpayPaymentUrlResult(
                    command.txnRef(),
                    paymentUrl,
                    objectMapper.writeValueAsString(payload),
                    true
            );
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize mock VNPay payload", ex);
        }
    }

    private SortedMap<String, String> buildBaseParams(VnpayCreatePaymentUrlCommand command) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnpayProperties.getTmnCode());
        params.put("vnp_Locale", "vn");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", command.txnRef());
        params.put("vnp_OrderInfo", command.orderDescription());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Amount", String.valueOf(toVnpayAmount(command.amount())));
        params.put("vnp_ReturnUrl", resolveReturnUrl(command));
        params.put("vnp_IpAddr", resolveClientIp(command.clientIp()));
        params.put("vnp_CreateDate", CREATE_DATE_FORMAT.format(command.occurredAt().atZone(VNPAY_ZONE)));
        return params;
    }

    private String resolveReturnUrl(VnpayCreatePaymentUrlCommand command) {
        if (!StringUtils.hasText(command.vnpayReturnUrl())) {
            throw new AppException(ErrorCode.VNPAY_PROVIDER_UNAVAILABLE, "VNPay return URL is not configured");
        }
        return command.vnpayReturnUrl().trim();
    }

    private long toVnpayAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT, "Payment amount must be greater than 0");
        }
        return amount.setScale(0, RoundingMode.HALF_UP).longValueExact() * 100L;
    }

    private String resolveClientIp(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return "127.0.0.1";
        }
        String trimmed = clientIp.trim();
        int commaIndex = trimmed.indexOf(',');
        if (commaIndex > 0) {
            return trimmed.substring(0, commaIndex).trim();
        }
        return trimmed;
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String toJson(SortedMap<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize VNPay request params", ex);
        }
    }
}
