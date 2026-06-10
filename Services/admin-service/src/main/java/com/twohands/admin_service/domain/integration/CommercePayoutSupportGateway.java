package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;
import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;

import java.util.Optional;
import java.util.UUID;

public interface CommercePayoutSupportGateway {

    boolean isEnabled();

    AdminPayoutRequestListResult listPayoutRequests(
            Optional<String> status,
            Integer page,
            Integer limit,
            String bearerToken
    );

    AdminPayoutRequestItem approvePayoutRequest(UUID payoutRequestId, String bearerToken);

    AdminPayoutRequestItem rejectPayoutRequest(UUID payoutRequestId, String adminNote, String bearerToken);

    AdminPayoutRequestItem markPayoutRequestPaid(UUID payoutRequestId, String bankTransferRef, String bearerToken);
}
