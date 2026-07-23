package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.CommerceRefundSupportGateway;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceRefundSupportGateway implements CommerceRefundSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommerceRefundSupportGateway.class);

	private final RestClient restClient;

	public HttpCommerceRefundSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(CommerceIntegrationJsonSupport.trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public AdminRefundApprovalListResult listRefundApprovals(
			Optional<String> status,
			Optional<String> q,
			Optional<String> requestedBy,
			Optional<String> paymentMethod,
			Optional<String> from,
			Optional<String> to,
			Integer page,
			Integer limit,
			String bearerToken
	) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromPath("/commerce/api/v1/admin/refund-approvals");
			status.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("status", value));
			q.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("q", value));
			requestedBy.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("requested_by", value));
			paymentMethod.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("payment_method", value));
			from.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("from", value));
			to.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("to", value));
			if (page != null) {
				builder.queryParam("page", page);
			}
			if (limit != null) {
				builder.queryParam("limit", limit);
			}

			JsonNode root = restClient.get()
					.uri(builder.build().toUriString())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return CommerceRefundSupportMapper.toListResult(root.path("data"));
		} catch (RestClientResponseException ex) {
			throw mapFailure(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce refund list failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public AdminRefundApprovalItem getRefundApproval(UUID refundRequestId, String bearerToken) {
		try {
			JsonNode root = restClient.get()
					.uri("/commerce/api/v1/admin/refund-approvals/{id}", refundRequestId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return CommerceRefundSupportMapper.toItem(root.path("data"));
		} catch (RestClientResponseException ex) {
			throw mapFailure(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce refund detail failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public AdminRefundApprovalItem confirmRefundApproval(UUID refundRequestId, String adminNote, String bearerToken) {
		return postAction(refundRequestId, "/confirm", Map.of("admin_note", adminNote == null ? "" : adminNote), bearerToken);
	}

	@Override
	public AdminRefundApprovalItem rejectRefundApproval(UUID refundRequestId, String adminNote, String bearerToken) {
		return postAction(refundRequestId, "/reject", Map.of("admin_note", adminNote == null ? "" : adminNote), bearerToken);
	}

	private AdminRefundApprovalItem postAction(
			UUID refundRequestId,
			String suffix,
			Map<String, Object> body,
			String bearerToken
	) {
		try {
			JsonNode root = restClient.post()
					.uri("/commerce/api/v1/admin/refund-approvals/{id}" + suffix, refundRequestId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return CommerceRefundSupportMapper.toItem(root.path("data"));
		} catch (RestClientResponseException ex) {
			throw mapFailure(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce refund action failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	private AppException mapFailure(RestClientResponseException ex) {
		if (ex.getStatusCode().value() == 404) {
			return new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
		}
		if (ex.getStatusCode().value() == 403) {
			return new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
		}
		log.warn("Commerce refund request failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
		return new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
	}
}
