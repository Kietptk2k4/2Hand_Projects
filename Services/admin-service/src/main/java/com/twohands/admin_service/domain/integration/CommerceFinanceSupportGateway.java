package com.twohands.admin_service.domain.integration;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public interface CommerceFinanceSupportGateway {

	boolean isEnabled();

	JsonNode fetchPlatformSummary(String from, String to, String bearerToken);

	JsonNode fetchPlatformRevenueTrend(String from, String to, String granularity, String bearerToken);

	JsonNode fetchCodPipeline(String bearerToken);

	JsonNode fetchTopSellers(String from, String to, Integer limit, String bearerToken);

	JsonNode fetchPayoutOverview(String from, String to, String bearerToken);

	JsonNode fetchSellerSummary(UUID sellerId, String from, String to, String bearerToken);

	JsonNode fetchSellerLedger(UUID sellerId, Integer page, Integer limit, String bearerToken);
}
