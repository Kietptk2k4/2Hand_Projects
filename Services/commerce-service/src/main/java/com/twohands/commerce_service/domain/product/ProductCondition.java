package com.twohands.commerce_service.domain.product;

import java.util.Set;

public final class ProductCondition {

    public static final String LIKE_NEW = "LIKE_NEW";
    public static final String GOOD = "GOOD";
    public static final String FAIR = "FAIR";
    public static final String USED = "USED";

    private static final Set<String> ALLOWED = Set.of(LIKE_NEW, GOOD, FAIR, USED);

    private ProductCondition() {
    }

    public static boolean isAllowed(String normalizedCondition) {
        return ALLOWED.contains(normalizedCondition);
    }

    public static String normalize(String rawCondition) {
        return rawCondition.trim().toUpperCase();
    }
}