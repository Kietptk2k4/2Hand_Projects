package com.twohands.admin_service.application.finance.viewsellerfinance;

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
public class ViewSellerFinanceSupportUseCase {

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceFinanceSupportGateway commerceFinanceSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewSellerFinanceSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceFinanceSupportGateway commerceFinanceSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceFinanceSupportGateway = commerceFinanceSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public JsonNode fetchSellerSummary(ViewSellerFinanceSupportQuery query) {
		UUID adminId = authorizeFinanceRead();
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		JsonNode data = commerceFinanceSupportGateway.fetchSellerSummary(
				query.sellerId(), query.from(), query.to(), query.bearerToken()
		);
		auditSellerView(adminId, query.sellerId(), "summary");
		return data;
	}

	@Transactional
	public JsonNode fetchSellerLedger(ViewSellerFinanceSupportQuery query) {
		UUID adminId = authorizeFinanceRead();
		FinanceSupportAccess.requireEnabled(commerceFinanceSupportGateway);
		JsonNode data = commerceFinanceSupportGateway.fetchSellerLedger(
				query.sellerId(), query.page(), query.limit(), query.bearerToken()
		);
		auditSellerView(adminId, query.sellerId(), "ledger");
		return data;
	}

	private UUID authorizeFinanceRead() {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.FINANCE_SUPPORT_READ);
		return adminId;
	}

	private void auditSellerView(UUID adminId, UUID sellerId, String view) {
		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.FINANCE_SELLER_DRILL_DOWN_VIEW.name(),
				AdminActionTargetType.PLATFORM_FINANCE,
				sellerId.toString(),
				"Seller finance drill-down viewed",
				Map.of("sellerId", sellerId.toString(), "view", view),
				Map.of("view", view)
		);
	}

	public String successMessage() {
		return "Seller finance data retrieved successfully";
	}
}
