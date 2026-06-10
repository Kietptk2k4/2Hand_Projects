package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.application.finance.viewsellerfinance.ViewSellerFinanceSupportQuery;
import com.twohands.admin_service.application.finance.viewsellerfinance.ViewSellerFinanceSupportUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/finance/sellers")
public class FinanceSellerController {

	private final ViewSellerFinanceSupportUseCase viewSellerFinanceSupportUseCase;

	public FinanceSellerController(ViewSellerFinanceSupportUseCase viewSellerFinanceSupportUseCase) {
		this.viewSellerFinanceSupportUseCase = viewSellerFinanceSupportUseCase;
	}

	@GetMapping("/{sellerId}/summary")
	@RequireAdminPermission(AdminPermission.FINANCE_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewSellerSummary(
			@PathVariable UUID sellerId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			HttpServletRequest request
	) {
		JsonNode data = viewSellerFinanceSupportUseCase.fetchSellerSummary(
				new ViewSellerFinanceSupportQuery(sellerId, from, to, null, null, bearerToken(request))
		);
		return ok(data);
	}

	@GetMapping("/{sellerId}/ledger")
	@RequireAdminPermission(AdminPermission.FINANCE_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewSellerLedger(
			@PathVariable UUID sellerId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer limit,
			HttpServletRequest request
	) {
		JsonNode data = viewSellerFinanceSupportUseCase.fetchSellerLedger(
				new ViewSellerFinanceSupportQuery(sellerId, null, null, page, limit, bearerToken(request))
		);
		return ok(data);
	}

	private ResponseEntity<ApiResponse<JsonNode>> ok(JsonNode data) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewSellerFinanceSupportUseCase.successMessage(),
				data
		));
	}

	private String bearerToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return "";
		}
		return authorization.substring(7).trim();
	}
}
