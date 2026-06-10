package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.CommercePayoutSupportGateway;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;
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
public class HttpCommercePayoutSupportGateway implements CommercePayoutSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommercePayoutSupportGateway.class);

	private final RestClient restClient;

	public HttpCommercePayoutSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(CommerceIntegrationJsonSupport.trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public AdminPayoutRequestListResult listPayoutRequests(
			Optional<String> status,
			Integer page,
			Integer limit,
			String bearerToken
	) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromPath("/commerce/api/v1/admin/finance/payout-requests");
			status.filter(value -> !value.isBlank()).ifPresent(value -> builder.queryParam("status", value));
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
			return CommercePayoutSupportMapper.toListResult(root.path("data"));
		} catch (RestClientResponseException ex) {
			throw mapFailure(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce payout list failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public AdminPayoutRequestItem approvePayoutRequest(UUID payoutRequestId, String bearerToken) {
		return postAction(payoutRequestId, "/approve", Map.of(), bearerToken);
	}

	@Override
	public AdminPayoutRequestItem rejectPayoutRequest(UUID payoutRequestId, String adminNote, String bearerToken) {
		return postAction(
				payoutRequestId,
				"/reject",
				Map.of("admin_note", adminNote == null ? "" : adminNote),
				bearerToken
		);
	}

	@Override
	public AdminPayoutRequestItem markPayoutRequestPaid(
			UUID payoutRequestId,
			String bankTransferRef,
			String bearerToken
	) {
		return postAction(payoutRequestId, "/mark-paid", Map.of("bank_transfer_ref", bankTransferRef), bearerToken);
	}

	private AdminPayoutRequestItem postAction(
			UUID payoutRequestId,
			String suffix,
			Map<String, Object> body,
			String bearerToken
	) {
		try {
			JsonNode root = restClient.post()
					.uri("/commerce/api/v1/admin/finance/payout-requests/{id}" + suffix, payoutRequestId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return CommercePayoutSupportMapper.toItem(root.path("data"));
		} catch (RestClientResponseException ex) {
			throw mapFailure(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce payout action failed: {}", ex.getMessage());
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
		log.warn("Commerce payout request failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
		return new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
	}
}
