package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
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

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceShipmentSupportGateway implements CommerceShipmentSupportGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpCommerceShipmentSupportGateway.class);

	private final RestClient restClient;

	public HttpCommerceShipmentSupportGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
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

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3003";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record CommerceShipmentSupportApiResponse(
			boolean success,
			CommerceShipmentSupportDetailPayload data
	) {
	}
}
