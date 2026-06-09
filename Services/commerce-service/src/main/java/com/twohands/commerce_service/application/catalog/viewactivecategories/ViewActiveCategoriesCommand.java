package com.twohands.commerce_service.application.catalog.viewactivecategories;

public record ViewActiveCategoriesCommand(
        Integer minLevel,
        Integer maxLevel,
        Boolean leafOnly,
        Boolean includeProductCounts
) {
    public boolean shouldIncludeProductCounts() {
        return includeProductCounts == null || includeProductCounts;
    }

    public boolean isLeafOnly() {
        return leafOnly != null && leafOnly;
    }
}
