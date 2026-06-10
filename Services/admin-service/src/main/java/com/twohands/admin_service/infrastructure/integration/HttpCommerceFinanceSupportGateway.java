package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.CommerceFinanceSupportGateway;
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

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceFinanceSupportGateway implements CommerceFinanceSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommerceFinanceSupportGateway.class);

	private final RestClient restClient;

	public HttpCommerceFinanceSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(CommerceIntegrationJsonSupport.trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public JsonNode fetchPlatformSummary(String from, String to, String bearerToken) {
		return get("/commerce/api/v1/admin/finance/platform/summary", bearerToken, builder -> {
			if (from != null && !from.isBlank()) builder.queryParam("from", from);
			if (to != null && !to.isBlank()) builder.queryParam("to", to);
		});
	}

	@Override
	public JsonNode fetchPlatformRevenueTrend(String from, String to, String granularity, String bearerToken) {
		return get("/commerce/api/v1/admin/finance/platform/revenue-trend", bearerToken, builder -> {
			if (from != null && !from.isBlank()) builder.queryParam("from", from);
			if (to != null && !to.isBlank()) builder.queryParam("to", to);
			if (granularity != null && !granularity.isBlank()) builder.queryParam("granularity", granularity);
		});
	}

	@Override
	public JsonNode fetchCodPipeline(String bearerToken) {
		return get("/commerce/api/v1/admin/finance/platform/cod-pipeline", bearerToken, builder -> {});
	}

	@Override
	public JsonNode fetchTopSellers(String from, String to, Integer limit, String bearerToken) {
		return get("/commerce/api/v1/admin/finance/platform/top-sellers", bearerToken, builder -> {
			if (from != null && !from.isBlank()) builder.queryParam("from", from);
			if (to != null && !to.isBlank()) builder.queryParam("to", to);
			if (limit != null) builder.queryParam("limit", limit);
		});
	}

	@Override
	public JsonNode fetchPayoutOverview(String from, String to, String bearerToken) {
		return get("/commerce/api/v1/admin/finance/platform/payout-overview", bearerToken, builder -> {
			if (from != null && !from.isBlank()) builder.queryParam("from", from);
			if (to != null && !to.isBlank()) builder.queryParam("to", to);
		});
	}

	@Override
	public JsonNode fetchSellerSummary(UUID sellerId, String from, String to, String bearerToken) {
		return get("/commerce/api/v1/admin/finance/sellers/" + sellerId + "/summary", bearerToken, builder -> {
			if (from != null && !from.isBlank()) builder.queryParam("from", from);
			if (to != null && !to.isBlank()) builder.queryParam("to", to);
		});
	}

	@Override
	public JsonNode fetchSellerLedger(UUID sellerId, Integer page, Integer limit, String bearerToken) {
		return get("/commerce/api/v1/admin/finance/sellers/" + sellerId + "/ledger", bearerToken, builder -> {
			if (page != null) builder.queryParam("page", page);
			if (limit != null) builder.queryParam("limit", limit);
		});
	}

	private JsonNode get(String path, String bearerToken, java.util.function.Consumer<UriComponentsBuilder> customizer) {
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromPath(path);
			customizer.accept(builder);
			JsonNode root = restClient.get()
					.uri(builder.build().toUriString())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return root.path("data");
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Commerce finance request failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce finance request failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}
}
