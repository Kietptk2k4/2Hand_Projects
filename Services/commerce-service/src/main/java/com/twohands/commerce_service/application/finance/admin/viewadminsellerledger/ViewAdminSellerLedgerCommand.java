package com.twohands.commerce_service.application.finance.admin.viewadminsellerledger;

import java.util.UUID;

public record ViewAdminSellerLedgerCommand(UUID sellerId, Integer page, Integer limit) {
}
