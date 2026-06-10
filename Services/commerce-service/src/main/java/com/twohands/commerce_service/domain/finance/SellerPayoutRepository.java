package com.twohands.commerce_service.domain.finance;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerPayoutRepository {

    List<SellerPayoutAccount> findAccountsBySellerId(UUID sellerId);

    Optional<SellerPayoutAccount> findAccountById(UUID sellerId, UUID accountId);

    UUID createAccount(
            UUID sellerId,
            String bankName,
            String bankAccountName,
            String bankAccountNumber,
            boolean isDefault,
            Instant now
    );

    void updateAccount(
            UUID sellerId,
            UUID accountId,
            String bankName,
            String bankAccountName,
            String bankAccountNumber,
            boolean isDefault,
            Instant now
    );

    void clearDefaultAccounts(UUID sellerId, Instant now);

    BigDecimal sumPendingPayoutAmount(UUID sellerId);

    long countPayoutRequests(UUID sellerId, Optional<PayoutRequestStatus> status);

    List<SellerPayoutRequest> findPayoutRequestsBySellerId(
            UUID sellerId,
            Optional<PayoutRequestStatus> status,
            PageQuery pageQuery
    );

    long countAdminPayoutRequests(Optional<PayoutRequestStatus> status);

    List<SellerPayoutRequest> findAdminPayoutRequests(Optional<PayoutRequestStatus> status, PageQuery pageQuery);

    Optional<SellerPayoutRequest> findPayoutRequestById(UUID payoutRequestId);

    Optional<SellerPayoutRequest> findPayoutRequestForSeller(UUID sellerId, UUID payoutRequestId);

    UUID createPayoutRequest(UUID sellerId, UUID payoutAccountId, BigDecimal amount, Instant now);

    boolean cancelPayoutRequest(UUID sellerId, UUID payoutRequestId, Instant now);

    boolean approvePayoutRequest(UUID payoutRequestId, Instant now);

    boolean rejectPayoutRequest(UUID payoutRequestId, String adminNote, Instant now);

    boolean markPayoutRequestPaid(UUID payoutRequestId, String bankTransferRef, Instant now);
}
