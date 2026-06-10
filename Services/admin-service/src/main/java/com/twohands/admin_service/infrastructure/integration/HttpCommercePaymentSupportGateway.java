package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommercePaymentSupportGateway implements CommercePaymentSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommercePaymentSupportGateway.class);

	private final RestClient restClient;

	public HttpCommercePaymentSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public PagedResult<PaymentSupportListEntry> searchPayments(
			String status,
			String paymentMethod,
			String orderId,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	) {
		try {
			CommercePaymentsSupportApiResponse body = restClient.get()
					.uri(uriBuilder -> {
						var builder = uriBuilder.path("/commerce/api/v1/admin/support/payments");
						if (StringUtils.hasText(status)) {
							builder.queryParam("status", status);
						}
						if (StringUtils.hasText(paymentMethod)) {
							builder.queryParam("payment_method", paymentMethod);
						}
						if (StringUtils.hasText(orderId)) {
							builder.queryParam("order_id", orderId);
						}
						if (StringUtils.hasText(from)) {
							builder.queryParam("from", from);
						}
						if (StringUtils.hasText(to)) {
							builder.queryParam("to", to);
						}
						if (page != null) {
							builder.queryParam("page", page);
						}
						if (size != null) {
							builder.queryParam("size", size);
						}
						return builder.build();
					})
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(CommercePaymentsSupportApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommercePaymentSupportListMapper.toDomain(body.data());
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 400) {
				throw new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Commerce payment support list failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce payment support list failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public PaymentSupportDetail fetchPaymentSupportDetail(UUID paymentId, String bearerToken) {
		try {
			CommercePaymentSupportApiResponse body = restClient.get()
					.uri("/commerce/api/v1/admin/support/payments/{paymentId}", paymentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(CommercePaymentSupportApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommercePaymentSupportDetailMapper.toDomain(body.data());
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Commerce payment support lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce payment support lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3003";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record CommercePaymentSupportApiResponse(
			boolean success,
			CommercePaymentSupportDetailPayload data
	) {
	}

	private record CommercePaymentsSupportApiResponse(
			boolean success,
			CommercePaymentsSupportPayload data
	) {
	}
}
