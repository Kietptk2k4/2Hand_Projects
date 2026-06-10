package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.application.finance.payout.PayoutRequestStatusParser;
import com.twohands.commerce_service.application.finance.payout.cancelsellerpayoutrequest.CancelSellerPayoutRequestCommand;
import com.twohands.commerce_service.application.finance.payout.cancelsellerpayoutrequest.CancelSellerPayoutRequestUseCase;
import com.twohands.commerce_service.application.finance.payout.createsellerpayoutaccount.CreateSellerPayoutAccountCommand;
import com.twohands.commerce_service.application.finance.payout.createsellerpayoutaccount.CreateSellerPayoutAccountUseCase;
import com.twohands.commerce_service.application.finance.payout.createsellerpayoutrequest.CreateSellerPayoutRequestCommand;
import com.twohands.commerce_service.application.finance.payout.createsellerpayoutrequest.CreateSellerPayoutRequestUseCase;
import com.twohands.commerce_service.application.finance.payout.listsellerpayoutaccounts.ListSellerPayoutAccountsCommand;
import com.twohands.commerce_service.application.finance.payout.listsellerpayoutaccounts.ListSellerPayoutAccountsUseCase;
import com.twohands.commerce_service.application.finance.payout.listsellerpayoutrequests.ListSellerPayoutRequestsCommand;
import com.twohands.commerce_service.application.finance.payout.listsellerpayoutrequests.ListSellerPayoutRequestsUseCase;
import com.twohands.commerce_service.application.finance.payout.updatesellerpayoutaccount.UpdateSellerPayoutAccountCommand;
import com.twohands.commerce_service.application.finance.payout.updatesellerpayoutaccount.UpdateSellerPayoutAccountUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.finance.SellerPayoutAccount;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutAccountsResult;
import com.twohands.commerce_service.domain.finance.ViewSellerPayoutRequestsResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/seller/finance")
public class SellerPayoutController {

    private final ListSellerPayoutAccountsUseCase listSellerPayoutAccountsUseCase;
    private final CreateSellerPayoutAccountUseCase createSellerPayoutAccountUseCase;
    private final UpdateSellerPayoutAccountUseCase updateSellerPayoutAccountUseCase;
    private final ListSellerPayoutRequestsUseCase listSellerPayoutRequestsUseCase;
    private final CreateSellerPayoutRequestUseCase createSellerPayoutRequestUseCase;
    private final CancelSellerPayoutRequestUseCase cancelSellerPayoutRequestUseCase;

    public SellerPayoutController(
            ListSellerPayoutAccountsUseCase listSellerPayoutAccountsUseCase,
            CreateSellerPayoutAccountUseCase createSellerPayoutAccountUseCase,
            UpdateSellerPayoutAccountUseCase updateSellerPayoutAccountUseCase,
            ListSellerPayoutRequestsUseCase listSellerPayoutRequestsUseCase,
            CreateSellerPayoutRequestUseCase createSellerPayoutRequestUseCase,
            CancelSellerPayoutRequestUseCase cancelSellerPayoutRequestUseCase
    ) {
        this.listSellerPayoutAccountsUseCase = listSellerPayoutAccountsUseCase;
        this.createSellerPayoutAccountUseCase = createSellerPayoutAccountUseCase;
        this.updateSellerPayoutAccountUseCase = updateSellerPayoutAccountUseCase;
        this.listSellerPayoutRequestsUseCase = listSellerPayoutRequestsUseCase;
        this.createSellerPayoutRequestUseCase = createSellerPayoutRequestUseCase;
        this.cancelSellerPayoutRequestUseCase = cancelSellerPayoutRequestUseCase;
    }

    @GetMapping("/payout-accounts")
    public ResponseEntity<ApiResponse<ViewSellerPayoutAccountsResponse>> listPayoutAccounts(Authentication authentication) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerPayoutAccountsResult result = listSellerPayoutAccountsUseCase.execute(
                new ListSellerPayoutAccountsCommand(sellerId)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listSellerPayoutAccountsUseCase.successMessage(),
                ViewSellerPayoutAccountsResponse.from(result)
        ));
    }

    @PostMapping("/payout-accounts")
    public ResponseEntity<ApiResponse<SellerPayoutAccountResponse>> createPayoutAccount(
            @RequestBody @Valid UpsertSellerPayoutAccountRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerPayoutAccount account = createSellerPayoutAccountUseCase.execute(
                new CreateSellerPayoutAccountCommand(
                        sellerId,
                        request.bankName(),
                        request.bankAccountName(),
                        request.bankAccountNumber(),
                        request.isDefault()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                createSellerPayoutAccountUseCase.successMessage(),
                SellerPayoutAccountResponse.from(account)
        ));
    }

    @PutMapping("/payout-accounts/{accountId}")
    public ResponseEntity<ApiResponse<SellerPayoutAccountResponse>> updatePayoutAccount(
            @PathVariable UUID accountId,
            @RequestBody @Valid UpsertSellerPayoutAccountRequest request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerPayoutAccount account = updateSellerPayoutAccountUseCase.execute(
                new UpdateSellerPayoutAccountCommand(
                        sellerId,
                        accountId,
                        request.bankName(),
                        request.bankAccountName(),
                        request.bankAccountNumber(),
                        request.isDefault()
                )
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateSellerPayoutAccountUseCase.successMessage(),
                SellerPayoutAccountResponse.from(account)
        ));
    }

    @GetMapping("/payout-requests")
    public ResponseEntity<ApiResponse<ViewSellerPayoutRequestsResponse>> listPayoutRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        ViewSellerPayoutRequestsResult result = listSellerPayoutRequestsUseCase.execute(
                new ListSellerPayoutRequestsCommand(
                        sellerId,
                        PayoutRequestStatusParser.parseOptional(status),
                        page,
                        limit
                )
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                listSellerPayoutRequestsUseCase.successMessage(),
                ViewSellerPayoutRequestsResponse.from(result)
        ));
    }

    @PostMapping("/payout-requests")
    public ResponseEntity<ApiResponse<SellerPayoutRequestResponse>> createPayoutRequest(
            @RequestBody @Valid CreateSellerPayoutRequestBody request,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerPayoutRequest payoutRequest = createSellerPayoutRequestUseCase.execute(
                new CreateSellerPayoutRequestCommand(sellerId, request.payoutAccountId(), request.amount())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                HttpStatus.CREATED.value(),
                createSellerPayoutRequestUseCase.successMessage(),
                SellerPayoutRequestResponse.from(payoutRequest)
        ));
    }

    @PostMapping("/payout-requests/{payoutRequestId}/cancel")
    public ResponseEntity<ApiResponse<SellerPayoutRequestResponse>> cancelPayoutRequest(
            @PathVariable UUID payoutRequestId,
            Authentication authentication
    ) {
        UUID sellerId = resolveUserId(authentication);
        SellerPayoutRequest payoutRequest = cancelSellerPayoutRequestUseCase.execute(
                new CancelSellerPayoutRequestCommand(sellerId, payoutRequestId)
        );
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                cancelSellerPayoutRequestUseCase.successMessage(),
                SellerPayoutRequestResponse.from(payoutRequest)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }
}
