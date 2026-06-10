package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;
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

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceShipmentSupportGateway implements CommerceShipmentSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommerceShipmentSupportGateway.class);

	private final RestClient restClient;

	public HttpCommerceShipmentSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(CommerceIntegrationJsonSupport.trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public ShipmentSupportDetail fetchShipmentSupportDetail(UUID shipmentId, String bearerToken) {
		try {
			CommerceShipmentSupportApiResponse body = restClient.get()
					.uri("/commerce/api/v1/admin/support/shipments/{shipmentId}", shipmentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(CommerceShipmentSupportApiResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
			}
			return CommerceShipmentSupportDetailMapper.toDomain(body.data());
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Commerce shipment support lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce shipment support lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public PagedResult<ShipmentSupportListEntry> listShipmentSupport(
			String status,
			String carrier,
			String sort,
			Integer page,
			Integer size,
			String bearerToken
	) {
		try {
			JsonNode root = restClient.get()
					.uri(uriBuilder -> {
						var builder = uriBuilder.path("/commerce/api/v1/admin/support/shipments");
						if (StringUtils.hasText(status)) {
							builder.queryParam("status", status);
						}
						if (StringUtils.hasText(carrier)) {
							builder.queryParam("carrier", carrier);
						}
						if (StringUtils.hasText(sort)) {
							builder.queryParam("sort", sort);
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
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return CommerceShipmentSupportListMapper.toDomain(root.path("data"));
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 400) {
				throw new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Commerce shipment support list failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Commerce shipment support list failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	@Override
	public AdminOverrideShipmentStatusResult overrideShipmentStatus(
			UUID shipmentId,
			String status,
			String reason,
			boolean force,
			String bearerToken
	) {
		try {
			JsonNode root = restClient.patch()
					.uri("/commerce/api/v1/admin/support/shipments/{shipmentId}/status", shipmentId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"status", status,
							"reason", reason,
							"force", force
					))
					.retrieve()
					.body(JsonNode.class);
			CommerceIntegrationJsonSupport.requireSuccess(root);
			return CommerceShipmentStatusOverrideMapper.toDomain(root.path("data"));
		} catch (RestClientResponseException ex) {
			throw mapOverrideFailure(ex);
		} catch (RestClientException ex) {
			log.warn("Commerce shipment status override failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
		}
	}

	private AppException mapOverrideFailure(RestClientResponseException ex) {
		int status = ex.getStatusCode().value();
		if (status == 404) {
			return new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
		}
		if (status == 403) {
			return new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
		}
		if (status == 400) {
			return new AppException(ErrorCode.VALIDATION_ERROR, resolveCommerceMessage(ex));
		}
		if (status == 409) {
			return new AppException(ErrorCode.SHIPMENT_STATUS_CONFLICT, resolveCommerceMessage(ex));
		}
		log.warn("Commerce shipment status override failed: status={}, message={}", status, ex.getMessage());
		return new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
	}

	private String resolveCommerceMessage(RestClientResponseException ex) {
		try {
			JsonNode root = ex.getResponseBodyAs(JsonNode.class);
			if (root != null && root.hasNonNull("message")) {
				String message = root.get("message").asText();
				if (!message.isBlank()) {
					return message;
				}
			}
		} catch (RuntimeException ignored) {
			// fall through to default message
		}
		return ex.getStatusText();
	}

	private record CommerceShipmentSupportApiResponse(
			boolean success,
			CommerceShipmentSupportDetailPayload data
	) {
	}
}
