package com.twohands.commerce_service.application.finance.recordsellerledgercredit;

import com.twohands.commerce_service.config.CommerceFinanceProperties;
import com.twohands.commerce_service.domain.finance.OrderItemLedgerSnapshot;
import com.twohands.commerce_service.domain.finance.SellerLedgerAmounts;
import com.twohands.commerce_service.domain.finance.SellerLedgerCommission;
import com.twohands.commerce_service.domain.finance.SellerLedgerCreditDraft;
import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RecordSellerLedgerCreditService {

    private final SellerLedgerRepository sellerLedgerRepository;
    private final CommerceFinanceProperties financeProperties;

    public RecordSellerLedgerCreditService(
            SellerLedgerRepository sellerLedgerRepository,
            CommerceFinanceProperties financeProperties
    ) {
        this.sellerLedgerRepository = sellerLedgerRepository;
        this.financeProperties = financeProperties;
    }

    public int recordCreditsForCompletedOrderItems(List<UUID> orderItemIds, Instant occurredAt) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            return 0;
        }

        BigDecimal commissionRate = financeProperties.getPlatformCommissionRate();
        int recorded = 0;
        for (OrderItemLedgerSnapshot snapshot : sellerLedgerRepository.findEligibleCreditSnapshots(orderItemIds)) {
            SellerLedgerAmounts amounts = SellerLedgerCommission.calculate(snapshot.finalPrice(), commissionRate);
            boolean inserted = sellerLedgerRepository.insertCreditIfAbsent(
                    new SellerLedgerCreditDraft(
                            snapshot.sellerId(),
                            snapshot.orderItemId(),
                            amounts,
                            occurredAt
                    )
            );
            if (inserted) {
                recorded++;
            }
        }
        return recorded;
    }
}
