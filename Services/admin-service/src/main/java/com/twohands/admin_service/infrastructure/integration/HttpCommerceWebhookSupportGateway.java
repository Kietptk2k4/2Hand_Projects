package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;
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
import org.springframework.web.util.UriBuilder;

import java.util.UUID;

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
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	) {
		try {
			CommerceWebhookSupportApiResponse body = restClient.get()
					.uri(uriBuilder -> buildSearchUri(uriBuilder, provider, referenceId, searchQuery, eventType, status, from, to, page, size))
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(CommerceWebhookSupportApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommerceWebhookSupportGatewayMapper.toDomain(body.data());
		} catch (RestClientResponseException ex) {
			throw mapResponseException(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce webhook logs lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public WebhookSupportLogStats fetchWebhookLogStats(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			String bearerToken
	) {
		try {
			CommerceWebhookStatsApiResponse body = restClient.get()
					.uri(uriBuilder -> {
						UriBuilder builder = uriBuilder.path("/commerce/api/v1/admin/support/webhook-logs/stats");
						applyFilterParams(builder, provider, referenceId, searchQuery, eventType, status, from, to);
						return builder.build();
					})
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(CommerceWebhookStatsApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommerceWebhookSupportGatewayMapper.toStats(body.data());
		} catch (RestClientResponseException ex) {
			throw mapResponseException(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce webhook stats lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public WebhookSupportLogEntry fetchWebhookLogDetail(UUID logId, String provider, String bearerToken) {
		try {
			CommerceWebhookDetailApiResponse body = restClient.get()
					.uri(uriBuilder -> uriBuilder
							.path("/commerce/api/v1/admin/support/webhook-logs/{logId}")
							.queryParam("provider", provider)
							.build(logId))
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(CommerceWebhookDetailApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommerceWebhookSupportGatewayMapper.toEntry(body.data());
		} catch (RestClientResponseException ex) {
			throw mapResponseException(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce webhook detail lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public byte[] exportWebhookLogsCsv(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			String bearerToken
	) {
		try {
			return restClient.get()
					.uri(uriBuilder -> {
						UriBuilder builder = uriBuilder.path("/commerce/api/v1/admin/support/webhook-logs/export");
						applyFilterParams(builder, provider, referenceId, searchQuery, eventType, status, from, to);
						builder.queryParam("format", "csv");
						return builder.build();
					})
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.valueOf("text/csv"))
					.retrieve()
					.body(byte[].class);
		} catch (RestClientResponseException ex) {
			throw mapResponseException(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce webhook export failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	private java.net.URI buildSearchUri(
			UriBuilder uriBuilder,
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			Integer page,
			Integer size
	) {
		UriBuilder builder = uriBuilder.path("/commerce/api/v1/admin/support/webhook-logs");
		applyFilterParams(builder, provider, referenceId, searchQuery, eventType, status, from, to);
		if (page != null) {
			builder.queryParam("page", page);
		}
		if (size != null) {
			builder.queryParam("size", size);
		}
		return builder.build();
	}

	private void applyFilterParams(
			UriBuilder builder,
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to
	) {
		if (StringUtils.hasText(provider)) {
			builder.queryParam("provider", provider);
		}
		if (StringUtils.hasText(referenceId)) {
			builder.queryParam("reference_id", referenceId);
		}
		if (StringUtils.hasText(searchQuery)) {
			builder.queryParam("q", searchQuery);
		}
		if (StringUtils.hasText(eventType)) {
			builder.queryParam("event_type", eventType);
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
	}

	private AppException mapResponseException(RestClientResponseException ex) {
		if (ex.getStatusCode().value() == 400) {
			return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage());
		}
		if (ex.getStatusCode().value() == 403) {
			return new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
		}
		if (ex.getStatusCode().value() == 404) {
			return new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
		}
		log.warn("Commerce webhook request failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
		return new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
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

	private record CommerceWebhookStatsApiResponse(
			boolean success,
			CommerceWebhookLogsStatsPayload data
	) {
	}

	private record CommerceWebhookDetailApiResponse(
			boolean success,
			CommerceWebhookLogsSupportPayload.WebhookLogPayload data
	) {
	}
}
