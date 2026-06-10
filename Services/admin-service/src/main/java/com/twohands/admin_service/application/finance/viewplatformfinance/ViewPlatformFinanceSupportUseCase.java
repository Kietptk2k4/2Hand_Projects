package com.twohands.admin_service.application.finance.viewplatformfinance;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.finance.FinanceSupportAccess;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceFinanceSupportGateway;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ViewPlatformFinanceSupportUseCase {

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceFinanceSupportGateway commerceFinanceSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewPlatformFinanceSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceFinanceSupportGateway commerceFinanceSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceFinanceSupportGateway = commerceFinanceSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public JsonNode fetchSummary(ViewPlatformFinanceSupportQuery query) {
		UUID adminId = authorizeFinanceRead();
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		JsonNode data = commerceFinanceSupportGateway.fetchPlatformSummary(query.from(), query.to(), query.bearerToken());
		auditView(adminId, "summary", query);
		return data;
	}

	@Transactional
	public JsonNode fetchRevenueTrend(ViewPlatformFinanceSupportQuery query) {
		UUID adminId = authorizeFinanceRead();
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		JsonNode data = commerceFinanceSupportGateway.fetchPlatformRevenueTrend(
				query.from(), query.to(), query.granularity(), query.bearerToken()
		);
		auditView(adminId, "revenue-trend", query);
		return data;
	}

	@Transactional
	public JsonNode fetchCodPipeline(String bearerToken) {
		UUID adminId = authorizeFinanceRead();
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		JsonNode data = commerceFinanceSupportGateway.fetchCodPipeline(bearerToken);
		auditView(adminId, "cod-pipeline", new ViewPlatformFinanceSupportQuery(null, null, null, null, bearerToken));
		return data;
	}

	@Transactional
	public JsonNode fetchTopSellers(ViewPlatformFinanceSupportQuery query) {
		UUID adminId = authorizeFinanceRead();
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		JsonNode data = commerceFinanceSupportGateway.fetchTopSellers(
				query.from(), query.to(), query.limit(), query.bearerToken()
		);
		auditView(adminId, "top-sellers", query);
		return data;
	}

	@Transactional
	public JsonNode fetchPayoutOverview(ViewPlatformFinanceSupportQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.PAYOUT_SUPPORT_READ);
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		return commerceFinanceSupportGateway.fetchPayoutOverview(query.from(), query.to(), query.bearerToken());
	}

	private UUID authorizeFinanceRead() {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.FINANCE_SUPPORT_READ);
		return adminId;
	}

	private void auditView(UUID adminId, String view, ViewPlatformFinanceSupportQuery query) {
		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.FINANCE_SUPPORT_VIEW.name(),
				AdminActionTargetType.PLATFORM_FINANCE,
				view,
				"Platform finance viewed",
				Map.of(
						"view", view,
						"from", query.from() != null ? query.from() : "",
						"to", query.to() != null ? query.to() : ""
				),
				Map.of("view", view)
		);
	}

	public String successMessage() {
		return "Platform finance data retrieved successfully";
	}
}
