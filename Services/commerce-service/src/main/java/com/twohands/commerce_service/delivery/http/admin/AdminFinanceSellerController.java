package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.finance.admin.viewadminsellerfinance.ViewAdminSellerFinanceSummaryCommand;
import com.twohands.commerce_service.application.finance.admin.viewadminsellerfinance.ViewAdminSellerFinanceSummaryUseCase;
import com.twohands.commerce_service.application.finance.admin.viewadminsellerledger.ViewAdminSellerLedgerCommand;
import com.twohands.commerce_service.application.finance.admin.viewadminsellerledger.ViewAdminSellerLedgerUseCase;
import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.seller.ViewSellerLedgerResponse;
import com.twohands.commerce_service.delivery.http.seller.ViewSellerRevenueSummaryResponse;
import com.twohands.commerce_service.domain.finance.SellerRevenueSummary;
import com.twohands.commerce_service.domain.finance.ViewSellerLedgerResult;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/finance/sellers")
public class AdminFinanceSellerController {

    private final ViewAdminSellerFinanceSummaryUseCase viewAdminSellerFinanceSummaryUseCase;
    private final ViewAdminSellerLedgerUseCase viewAdminSellerLedgerUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminFinanceSellerController(
            ViewAdminSellerFinanceSummaryUseCase viewAdminSellerFinanceSummaryUseCase,
            ViewAdminSellerLedgerUseCase viewAdminSellerLedgerUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewAdminSellerFinanceSummaryUseCase = viewAdminSellerFinanceSummaryUseCase;
        this.viewAdminSellerLedgerUseCase = viewAdminSellerLedgerUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping("/{sellerId}/summary")
    public ResponseEntity<ApiResponse<ViewSellerRevenueSummaryResponse>> viewSellerSummary(
            @PathVariable UUID sellerId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        requireFinanceRead(authentication);
        SellerRevenueSummary result = viewAdminSellerFinanceSummaryUseCase.execute(
                new ViewAdminSellerFinanceSummaryCommand(sellerId, parseFrom(from), parseTo(to))
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewAdminSellerFinanceSummaryUseCase.successMessage(),
                ViewSellerRevenueSummaryResponse.from(result)
        ));
    }

    @GetMapping("/{sellerId}/ledger")
    public ResponseEntity<ApiResponse<ViewSellerLedgerResponse>> viewSellerLedger(
            @PathVariable UUID sellerId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        requireFinanceRead(authentication);
        ViewSellerLedgerResult result = viewAdminSellerLedgerUseCase.execute(
                new ViewAdminSellerLedgerCommand(sellerId, page, limit)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewAdminSellerLedgerUseCase.successMessage(),
                ViewSellerLedgerResponse.from(result)
        ));
    }

    private void requireFinanceRead(Authentication authentication) {
        AuthenticatedUser admin = resolveUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
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
}
