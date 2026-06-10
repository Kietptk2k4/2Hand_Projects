package com.twohands.commerce_service.application.finance.payout.createsellerpayoutaccount;

import java.util.UUID;

public record CreateSellerPayoutAccountCommand(
        UUID sellerId,
        String bankName,
        String bankAccountName,
        String bankAccountNumber,
        boolean isDefault
) {
}
