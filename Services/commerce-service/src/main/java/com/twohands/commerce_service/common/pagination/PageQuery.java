package com.twohands.commerce_service.common.pagination;

public record PageQuery(
        int page,
        int limit
) {
    public int offset() {
        return (page - 1) * limit;
    }
}
