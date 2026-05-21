package com.twohands.commerce_service.common.pagination;

public record PageMeta(
        int page,
        int limit,
        long totalItems,
        int totalPages,
        boolean hasNext
) {
    public static PageMeta of(int page, int limit, long totalItems) {
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / limit);
        boolean hasNext = page < totalPages;
        return new PageMeta(page, limit, totalItems, totalPages, hasNext);
    }
}
