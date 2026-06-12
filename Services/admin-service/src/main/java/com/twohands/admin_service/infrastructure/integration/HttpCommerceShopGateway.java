package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.CommerceShopGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import java.util.Map;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceShopGateway implements CommerceShopGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommerceShopGateway.class);

	private final RestClient restClient;

	public HttpCommerceShopGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(CommerceIntegrationJsonSupport.trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void suspendShop(UUID shopId, UUID adminId, String reason) {
		moderateShop(shopId, adminId, "SUSPEND", reason);
	}

	@Override
	public void closeShop(UUID shopId, UUID adminId, String reason) {
		moderateShop(shopId, adminId, "CLOSE", reason);
	}

	@Override
	public void restoreShop(UUID shopId, UUID adminId, String reason) {
		moderateShop(shopId, adminId, "RESTORE", reason);
	}

	private void moderateShop(UUID shopId, UUID adminId, String action, String reason) {
		try {
			JsonNode root = restClient.post()
					.uri("/commerce/api/v1/internal/moderation/shops/{shopId}/moderate", shopId)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"moderated_by_admin_id", adminId.toString(),
							"action", action,
							"reason", reason
					))
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			if (ex.getStatusCode().value() == 409) {
				throw new AppException(ErrorCode.BAD_REQUEST, "Commerce rejected shop moderation");
			}
			log.warn("Commerce shop moderation failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce shop moderation failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public Optional<UUID> findShopOwnerId(UUID shopId) {
		try {
			JsonNode root = restClient.get()
					.uri("/commerce/api/v1/internal/moderation/shops/{shopId}", shopId)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			JsonNode data = root.path("data");
			if (data.isMissingNode() || data.isNull()) {
				return Optional.empty();
			}
			return Optional.ofNullable(CommerceIntegrationJsonSupport.parseUuid(data, "seller_id"));
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			log.warn("Commerce shop owner lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce shop owner lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}
}
