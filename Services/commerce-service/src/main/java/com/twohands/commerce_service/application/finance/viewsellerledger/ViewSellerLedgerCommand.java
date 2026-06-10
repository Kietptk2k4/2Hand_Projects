package com.twohands.commerce_service.application.finance.viewsellerledger;

import java.util.UUID;

public record ViewSellerLedgerCommand(
        UUID sellerId,
        Integer page,
        Integer limit
) {
}
