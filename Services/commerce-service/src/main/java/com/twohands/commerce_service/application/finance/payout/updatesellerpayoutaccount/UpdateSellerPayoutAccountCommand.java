package com.twohands.commerce_service.application.finance.payout.updatesellerpayoutaccount;

import java.util.UUID;

public record UpdateSellerPayoutAccountCommand(
        UUID sellerId,
        UUID accountId,
        String bankName,
        String bankAccountName,
        String bankAccountNumber,
        boolean isDefault
) {
}
