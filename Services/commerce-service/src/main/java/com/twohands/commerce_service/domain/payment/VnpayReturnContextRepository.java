package com.twohands.commerce_service.domain.payment;

import java.util.Optional;

public interface VnpayReturnContextRepository {

    Optional<String> findFrontendReturnUrlByTxnRef(String txnRef);
}
