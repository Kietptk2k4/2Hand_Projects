package com.twohands.commerce_service.application.finance.payout.listadminpayoutrequests;

import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;

import java.util.Optional;

public record ListAdminPayoutRequestsCommand(Optional<PayoutRequestStatus> status, Integer page, Integer limit) {
}
