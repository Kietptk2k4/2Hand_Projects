package com.twohands.commerce_service.application.catalog.common;

import java.util.UUID;

public final class CategoryPathCalculator {

    private CategoryPathCalculator() {
    }

    public static String buildPath(UUID categoryId, String parentPath) {
        if (parentPath == null || parentPath.isBlank()) {
            return "/" + categoryId + "/";
        }
        String normalizedParent = parentPath.endsWith("/") ? parentPath : parentPath + "/";
        return normalizedParent + categoryId + "/";
    }

    public static int resolveLevel(String parentPath) {
        if (parentPath == null || parentPath.isBlank()) {
            return 0;
        }
        String trimmed = parentPath.startsWith("/") ? parentPath.substring(1) : parentPath;
        if (trimmed.isBlank()) {
            return 0;
        }
        return (int) trimmed.chars().filter(ch -> ch == '/').count();
    }
}
