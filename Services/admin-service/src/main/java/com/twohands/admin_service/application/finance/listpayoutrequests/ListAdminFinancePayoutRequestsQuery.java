package com.twohands.admin_service.application.finance.listpayoutrequests;

import java.util.Optional;

public record ListAdminFinancePayoutRequestsQuery(
        Optional<String> status,
        Integer page,
        Integer limit,
        String bearerToken
) {
}
