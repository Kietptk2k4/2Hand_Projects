package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.application.finance.platform.viewplatformcodpipeline.ViewPlatformCodPipelineUseCase;
import com.twohands.commerce_service.application.finance.platform.viewplatformpayoutoverview.ViewPlatformPayoutOverviewCommand;
import com.twohands.commerce_service.application.finance.platform.viewplatformpayoutoverview.ViewPlatformPayoutOverviewUseCase;
import com.twohands.commerce_service.application.finance.platform.viewplatformrevenuetrend.ViewPlatformRevenueTrendCommand;
import com.twohands.commerce_service.application.finance.platform.viewplatformrevenuetrend.ViewPlatformRevenueTrendUseCase;
import com.twohands.commerce_service.application.finance.platform.viewplatformsummary.ViewPlatformFinanceSummaryCommand;
import com.twohands.commerce_service.application.finance.platform.viewplatformsummary.ViewPlatformFinanceSummaryUseCase;
import com.twohands.commerce_service.application.finance.platform.viewplatformtopsellers.ViewPlatformTopSellersCommand;
import com.twohands.commerce_service.application.finance.platform.viewplatformtopsellers.ViewPlatformTopSellersUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.finance.PlatformCodPipeline;
import com.twohands.commerce_service.domain.finance.PlatformFinanceSummary;
import com.twohands.commerce_service.domain.finance.PlatformPayoutStatusOverview;
import com.twohands.commerce_service.domain.finance.PlatformRevenueTrendResult;
import com.twohands.commerce_service.domain.finance.PlatformTopSeller;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/commerce/api/v1/admin/finance/platform")
public class AdminFinancePlatformController {

    private final ViewPlatformFinanceSummaryUseCase viewPlatformFinanceSummaryUseCase;
    private final ViewPlatformRevenueTrendUseCase viewPlatformRevenueTrendUseCase;
    private final ViewPlatformCodPipelineUseCase viewPlatformCodPipelineUseCase;
    private final ViewPlatformTopSellersUseCase viewPlatformTopSellersUseCase;
    private final ViewPlatformPayoutOverviewUseCase viewPlatformPayoutOverviewUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminFinancePlatformController(
            ViewPlatformFinanceSummaryUseCase viewPlatformFinanceSummaryUseCase,
            ViewPlatformRevenueTrendUseCase viewPlatformRevenueTrendUseCase,
            ViewPlatformCodPipelineUseCase viewPlatformCodPipelineUseCase,
            ViewPlatformTopSellersUseCase viewPlatformTopSellersUseCase,
            ViewPlatformPayoutOverviewUseCase viewPlatformPayoutOverviewUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewPlatformFinanceSummaryUseCase = viewPlatformFinanceSummaryUseCase;
        this.viewPlatformRevenueTrendUseCase = viewPlatformRevenueTrendUseCase;
        this.viewPlatformCodPipelineUseCase = viewPlatformCodPipelineUseCase;
        this.viewPlatformTopSellersUseCase = viewPlatformTopSellersUseCase;
        this.viewPlatformPayoutOverviewUseCase = viewPlatformPayoutOverviewUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PlatformFinanceSummaryResponse>> viewSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        requireFinanceRead(authentication);
        PlatformFinanceSummary result = viewPlatformFinanceSummaryUseCase.execute(
                new ViewPlatformFinanceSummaryCommand(parseFrom(from), parseTo(to))
        );
        return ok(viewPlatformFinanceSummaryUseCase.successMessage(), PlatformFinanceSummaryResponse.from(result));
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<ApiResponse<ViewPlatformRevenueTrendResponse>> viewRevenueTrend(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String granularity,
            Authentication authentication
    ) {
        requireFinanceRead(authentication);
        PlatformRevenueTrendResult result = viewPlatformRevenueTrendUseCase.execute(
                new ViewPlatformRevenueTrendCommand(
                        parseFrom(from),
                        parseTo(to),
                        ViewPlatformRevenueTrendUseCase.parseGranularity(granularity)
                )
        );
        return ok(viewPlatformRevenueTrendUseCase.successMessage(), ViewPlatformRevenueTrendResponse.from(result));
    }

    @GetMapping("/cod-pipeline")
    public ResponseEntity<ApiResponse<PlatformCodPipelineResponse>> viewCodPipeline(Authentication authentication) {
        requireFinanceRead(authentication);
        PlatformCodPipeline result = viewPlatformCodPipelineUseCase.execute();
        return ok(viewPlatformCodPipelineUseCase.successMessage(), PlatformCodPipelineResponse.from(result));
    }

    @GetMapping("/top-sellers")
    public ResponseEntity<ApiResponse<ViewPlatformTopSellersResponse>> viewTopSellers(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        requireFinanceRead(authentication);
        List<PlatformTopSeller> result = viewPlatformTopSellersUseCase.execute(
                new ViewPlatformTopSellersCommand(parseFrom(from), parseTo(to), limit)
        );
        return ok(viewPlatformTopSellersUseCase.successMessage(), ViewPlatformTopSellersResponse.from(result));
    }

    @GetMapping("/payout-overview")
    public ResponseEntity<ApiResponse<PlatformPayoutOverviewResponse>> viewPayoutOverview(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveUser(authentication);
        commerceAdminAuthorization.requirePermission(admin, CommerceAdminAuthorization.PERMISSION_PAYOUT_SUPPORT_READ);
        List<PlatformPayoutStatusOverview> result = viewPlatformPayoutOverviewUseCase.execute(
                new ViewPlatformPayoutOverviewCommand(parseFrom(from), parseTo(to))
        );
        return ok(viewPlatformPayoutOverviewUseCase.successMessage(), PlatformPayoutOverviewResponse.from(result));
    }

    private void requireFinanceRead(Authentication authentication) {
        commerceAdminAuthorization.requirePermission(
                resolveUser(authentication),
                CommerceAdminAuthorization.PERMISSION_FINANCE_SUPPORT_READ
        );
    }

    private AuthenticatedUser resolveUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }

    private Optional<Instant> parseFrom(String from) {
        return FinanceDateRangeResolver.parseOptionalInstant(from, "from");
    }

    private Optional<Instant> parseTo(String to) {
        return FinanceDateRangeResolver.parseOptionalInstant(to, "to");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, data));
    }
}
