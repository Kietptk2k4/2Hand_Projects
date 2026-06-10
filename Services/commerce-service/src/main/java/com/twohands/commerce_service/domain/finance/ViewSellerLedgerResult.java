package com.twohands.commerce_service.domain.finance;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;

public record ViewSellerLedgerResult(
        List<SellerLedgerListEntry> items,
        PageMeta pagination
) {
}
