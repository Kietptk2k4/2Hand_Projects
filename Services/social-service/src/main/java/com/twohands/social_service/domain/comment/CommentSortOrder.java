package com.twohands.social_service.domain.comment;

public enum CommentSortOrder {
    CREATED_AT_ASC,
    CREATED_AT_DESC;

    public static CommentSortOrder fromQueryParam(String sort) {
        if (sort == null || sort.isBlank() || "created_at_asc".equalsIgnoreCase(sort)) {
            return CREATED_AT_ASC;
        }
        if ("created_at_desc".equalsIgnoreCase(sort)) {
            return CREATED_AT_DESC;
        }
        return null;
    }
}
