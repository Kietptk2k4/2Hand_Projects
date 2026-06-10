package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.finance.common.FinanceDateRangeResolver;
import com.twohands.commerce_service.application.finance.viewsellerledger.ViewSellerLedgerCommand;
import com.twohands.commerce_service.application.finance.viewsellerledger.ViewSellerLedgerUseCase;
import com.twohands.commerce_service.application.finance.viewsellerrevenuesummary.ViewSellerRevenueSummaryCommand;
import com.twohands.commerce_service.application.finance.viewsellerrevenuesummary.ViewSellerRevenueSummaryUseCase;
import com.twohands.commerce_service.application.finance.viewsellerrevenuetrend.ViewSellerRevenueTrendCommand;
import com.twohands.commerce_service.application.finance.viewsellerrevenuetrend.ViewSellerRevenueTrendUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;
import com.twohands.commerce_service.domain.finance.SellerRevenueSummary;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendResult;
import com.twohands.commerce_service.domain.finance.ViewSellerLedgerResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/finance")
public class SellerFinanceController {

    private final ViewSellerRevenueSummaryUseCase viewSellerRevenueSummaryUseCase;
    private final ViewSellerRevenueTrendUseCase viewSellerRevenueTrendUseCase;
    private final ViewSellerLedgerUseCase viewSellerLedgerUseCase;

    public SellerFinanceController(
            ViewSellerRevenueSummaryUseCase viewSellerRevenueSummaryUseCase,
            ViewSellerRevenueTrendUseCase viewSellerRevenueTrendUseCase,
            ViewSellerLedgerUseCase viewSellerLedgerUseCase
    ) {
        this.viewSellerRevenueSummaryUseCase = viewSellerRevenueSummaryUseCase;
        this.viewSellerRevenueTrendUseCase = viewSellerRevenueTrendUseCase;
        this.viewSellerLedgerUseCase = viewSellerLedgerUseCase;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ViewSellerRevenueSummaryResponse>> viewRevenueSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        Optional<Instant> fromInstant = FinanceDateRangeResolver.parseOptionalInstant(from, "from");
        Optional<Instant> toInstant = FinanceDateRangeResolver.parseOptionalInstant(to, "to");

        SellerRevenueSummary result = viewSellerRevenueSummaryUseCase.execute(
                new ViewSellerRevenueSummaryCommand(sellerId, fromInstant, toInstant)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerRevenueSummaryUseCase.successMessage(),
                ViewSellerRevenueSummaryResponse.from(result)
        ));
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<ApiResponse<ViewSellerRevenueTrendResponse>> viewRevenueTrend(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String granularity,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        RevenueTrendGranularity parsedGranularity = ViewSellerRevenueTrendUseCase.parseGranularity(granularity);
        Optional<Instant> fromInstant = FinanceDateRangeResolver.parseOptionalInstant(from, "from");
        Optional<Instant> toInstant = FinanceDateRangeResolver.parseOptionalInstant(to, "to");

        SellerRevenueTrendResult result = viewSellerRevenueTrendUseCase.execute(
                new ViewSellerRevenueTrendCommand(sellerId, fromInstant, toInstant, parsedGranularity)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerRevenueTrendUseCase.successMessage(),
                ViewSellerRevenueTrendResponse.from(result)
        ));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<ViewSellerLedgerResponse>> viewLedger(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerLedgerResult result = viewSellerLedgerUseCase.execute(
                new ViewSellerLedgerCommand(sellerId, page, limit)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewSellerLedgerUseCase.successMessage(),
                ViewSellerLedgerResponse.from(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
