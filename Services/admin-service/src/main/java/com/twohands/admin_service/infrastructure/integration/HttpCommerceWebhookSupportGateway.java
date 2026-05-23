package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
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
@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceWebhookSupportGateway implements CommerceWebhookSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommerceWebhookSupportGateway.class);

	private final RestClient restClient;

	public HttpCommerceWebhookSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public PagedResult<WebhookSupportLogEntry> searchWebhookLogs(
			String provider,
			String referenceId,
			String status,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	) {
		try {
			CommerceWebhookSupportApiResponse body = restClient.get()
					.uri(uriBuilder -> {
						var builder = uriBuilder.path("/commerce/api/v1/admin/support/webhook-logs");
						if (StringUtils.hasText(provider)) {
							builder.queryParam("provider", provider);
						}
						if (StringUtils.hasText(referenceId)) {
							builder.queryParam("reference_id", referenceId);
						}
						if (StringUtils.hasText(status)) {
							builder.queryParam("status", status);
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
					.body(CommerceWebhookSupportApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommerceWebhookSupportGatewayMapper.toDomain(body.data());
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 400) {
				throw new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Commerce webhook logs lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce webhook logs lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3003";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record CommerceWebhookSupportApiResponse(
			boolean success,
			CommerceWebhookLogsSupportPayload data
	) {
	}
}
