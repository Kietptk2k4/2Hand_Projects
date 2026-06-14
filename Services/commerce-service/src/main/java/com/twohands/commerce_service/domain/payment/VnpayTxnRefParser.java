package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VnpayTxnRefParser {

    private static final Pattern TXN_REF_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})-(\\d+)$"
    );

    private VnpayTxnRefParser() {
    }

    public static String buildTxnRef(UUID orderId, Instant occurredAt) {
        return orderId + "-" + occurredAt.toEpochMilli();
    }

    public static UUID parseOrderId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }
        Matcher matcher = TXN_REF_PATTERN.matcher(txnRef.trim());
        if (!matcher.matches()) {
            return null;
        }
        return UUID.fromString(matcher.group(1));
    }
}
