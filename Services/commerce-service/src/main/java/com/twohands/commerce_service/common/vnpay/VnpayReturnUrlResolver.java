package com.twohands.commerce_service.common.vnpay;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class VnpayReturnUrlResolver {

    private final CommerceIntegrationProperties.Vnpay vnpayProperties;
    private final VnpayFrontendReturnUrlValidator returnUrlValidator;

    public VnpayReturnUrlResolver(
            CommerceIntegrationProperties integrationProperties,
            VnpayFrontendReturnUrlValidator returnUrlValidator
    ) {
        this.vnpayProperties = integrationProperties.getVnpay();
        this.returnUrlValidator = returnUrlValidator;
    }

    public String resolveBackendReturnUrl(HttpServletRequest request, String requestedReturnUrl) {
        if (StringUtils.hasText(requestedReturnUrl)) {
            String trimmed = requestedReturnUrl.trim();
            if (!returnUrlValidator.isAllowedBackendReturnUrl(trimmed, request)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "vnpay_return_url is not allowed");
            }
            return trimmed;
        }

        String detectedReturnUrl = ServletUriComponentsBuilder.fromRequest(request)
                .replacePath(VnpayFrontendReturnUrlValidator.BACKEND_RETURN_PATH)
                .replaceQuery(null)
                .build()
                .toUriString();
        if (returnUrlValidator.isAllowedBackendReturnUrl(detectedReturnUrl, request)) {
            return detectedReturnUrl;
        }

        if (StringUtils.hasText(vnpayProperties.getReturnUrl())) {
            return vnpayProperties.getReturnUrl().trim();
        }

        throw new AppException(ErrorCode.VNPAY_PROVIDER_UNAVAILABLE, "VNPay return URL is not configured");
    }

    public String resolveFrontendReturnUrl(String requestedFrontendReturnUrl) {
        if (!StringUtils.hasText(requestedFrontendReturnUrl)) {
            return null;
        }

        String trimmed = requestedFrontendReturnUrl.trim();
        if (!returnUrlValidator.isAllowedFrontendReturnUrl(trimmed)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "frontend_return_url is not allowed");
        }
        return trimmed;
    }
}
