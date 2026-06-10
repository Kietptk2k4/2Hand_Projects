package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.CommerceFinanceSupportGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceFinanceSupportGateway implements CommerceFinanceSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	private AppException disabled() {
		return new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; finance support is unavailable"
		);
	}

	@Override
	public JsonNode fetchPlatformSummary(String from, String to, String bearerToken) {
		throw disabled();
	}

	@Override
	public JsonNode fetchPlatformRevenueTrend(String from, String to, String granularity, String bearerToken) {
		throw disabled();
	}

	@Override
	public JsonNode fetchCodPipeline(String bearerToken) {
		throw disabled();
	}

	@Override
	public JsonNode fetchTopSellers(String from, String to, Integer limit, String bearerToken) {
		throw disabled();
	}

	@Override
	public JsonNode fetchPayoutOverview(String from, String to, String bearerToken) {
		throw disabled();
	}

	@Override
	public JsonNode fetchSellerSummary(UUID sellerId, String from, String to, String bearerToken) {
		throw disabled();
	}

	@Override
	public JsonNode fetchSellerLedger(UUID sellerId, Integer page, Integer limit, String bearerToken) {
		throw disabled();
	}
}
