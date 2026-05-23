package com.twohands.social_service.application.user.viewuserposts;

public enum UserPostsStatusFilter {
    PUBLISHED,
    ALL;

    public static UserPostsStatusFilter fromQueryParam(String value) {
        if (value == null || value.isBlank() || "published".equalsIgnoreCase(value)) {
            return PUBLISHED;
        }
        if ("all".equalsIgnoreCase(value)) {
            return ALL;
        }
        return null;
    }

    public String queryValue() {
        return this == ALL ? "all" : "published";
    }
}
