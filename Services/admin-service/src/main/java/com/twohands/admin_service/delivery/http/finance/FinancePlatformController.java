package com.twohands.admin_service.delivery.http.finance;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.application.finance.viewplatformfinance.ViewPlatformFinanceSupportQuery;
import com.twohands.admin_service.application.finance.viewplatformfinance.ViewPlatformFinanceSupportUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/finance/platform")
public class FinancePlatformController {

	private final ViewPlatformFinanceSupportUseCase viewPlatformFinanceSupportUseCase;

	public FinancePlatformController(ViewPlatformFinanceSupportUseCase viewPlatformFinanceSupportUseCase) {
		this.viewPlatformFinanceSupportUseCase = viewPlatformFinanceSupportUseCase;
	}

	@GetMapping("/summary")
	@RequireAdminPermission(AdminPermission.FINANCE_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewSummary(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			HttpServletRequest request
	) {
		JsonNode data = viewPlatformFinanceSupportUseCase.fetchSummary(
				new ViewPlatformFinanceSupportQuery(from, to, null, null, bearerToken(request))
		);
		return ok(data);
	}

	@GetMapping("/revenue-trend")
	@RequireAdminPermission(AdminPermission.FINANCE_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewRevenueTrend(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) String granularity,
			HttpServletRequest request
	) {
		JsonNode data = viewPlatformFinanceSupportUseCase.fetchRevenueTrend(
				new ViewPlatformFinanceSupportQuery(from, to, granularity, null, bearerToken(request))
		);
		return ok(data);
	}

	@GetMapping("/cod-pipeline")
	@RequireAdminPermission(AdminPermission.FINANCE_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewCodPipeline(HttpServletRequest request) {
		JsonNode data = viewPlatformFinanceSupportUseCase.fetchCodPipeline(bearerToken(request));
		return ok(data);
	}

	@GetMapping("/top-sellers")
	@RequireAdminPermission(AdminPermission.FINANCE_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewTopSellers(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) Integer limit,
			HttpServletRequest request
	) {
		JsonNode data = viewPlatformFinanceSupportUseCase.fetchTopSellers(
				new ViewPlatformFinanceSupportQuery(from, to, null, limit, bearerToken(request))
		);
		return ok(data);
	}

	@GetMapping("/payout-overview")
	@RequireAdminPermission(AdminPermission.PAYOUT_SUPPORT_READ)
	public ResponseEntity<ApiResponse<JsonNode>> viewPayoutOverview(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			HttpServletRequest request
	) {
		JsonNode data = viewPlatformFinanceSupportUseCase.fetchPayoutOverview(
				new ViewPlatformFinanceSupportQuery(from, to, null, null, bearerToken(request))
		);
		return ok(data);
	}

	private ResponseEntity<ApiResponse<JsonNode>> ok(JsonNode data) {
		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewPlatformFinanceSupportUseCase.successMessage(),
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
