package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerPayoutRequestResponse(
        @JsonProperty("id") UUID id,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("payout_account_id") UUID payoutAccountId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("status") PayoutRequestStatus status,
        @JsonProperty("admin_note") String adminNote,
        @JsonProperty("bank_transfer_ref") String bankTransferRef,
        @JsonProperty("requested_at") Instant requestedAt,
        @JsonProperty("approved_at") Instant approvedAt,
        @JsonProperty("paid_at") Instant paidAt,
        @JsonProperty("rejected_at") Instant rejectedAt,
        @JsonProperty("cancelled_at") Instant cancelledAt,
        @JsonProperty("bank_name") String bankName,
        @JsonProperty("bank_account_name") String bankAccountName,
        @JsonProperty("bank_account_number") String bankAccountNumber
) {
    public static SellerPayoutRequestResponse from(SellerPayoutRequest request) {
        return new SellerPayoutRequestResponse(
                request.id(),
                request.sellerId(),
                request.payoutAccountId(),
                request.amount(),
                request.status(),
                request.adminNote(),
                request.bankTransferRef(),
                request.requestedAt(),
                request.approvedAt(),
                request.paidAt(),
                request.rejectedAt(),
                request.cancelledAt(),
                request.bankName(),
                request.bankAccountName(),
                request.bankAccountNumber()
        );
    }
}
