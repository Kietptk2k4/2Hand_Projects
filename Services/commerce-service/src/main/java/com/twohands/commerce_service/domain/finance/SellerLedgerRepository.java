package com.twohands.commerce_service.domain.finance;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SellerLedgerRepository {

    List<OrderItemLedgerSnapshot> findEligibleCreditSnapshots(List<UUID> orderItemIds);

    boolean insertCreditIfAbsent(SellerLedgerCreditDraft draft);

    boolean insertDebitForPayout(UUID sellerId, UUID payoutRequestId, BigDecimal amount, Instant createdAt);

    SellerBalanceSummary findBalanceSummary(UUID sellerId);

    long countLedgerEntries(UUID sellerId);

    List<SellerLedgerListEntry> findLedgerEntries(UUID sellerId, PageQuery pageQuery);
}
