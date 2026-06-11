package com.twohands.social_service.application.admin.common;

import com.twohands.social_service.domain.admin.AdminCommentListSortField;
import com.twohands.social_service.domain.admin.AdminModerationListSortField;
import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;

import java.util.Locale;
import java.util.Set;

public final class AdminModerationListQueryPolicy {

    private static final Set<String> POST_STATUSES = Set.of("ACTIVE", "DRAFT", "DELETED");
    private static final Set<String> POST_MODERATION_STATUSES = Set.of("NONE", "HIDDEN", "REMOVED");
    private static final Set<String> COMMENT_STATUSES = Set.of("ACTIVE", "DELETED");

    private AdminModerationListQueryPolicy() {
    }

    public static AdminModerationListSortField parsePostSortField(String rawSort) {
        return parsePostSortField(rawSort, AdminModerationListSortField.CREATED_AT);
    }

    public static AdminModerationListSortField parsePostSortField(
            String rawSort,
            AdminModerationListSortField defaultField
    ) {
        if (rawSort == null || rawSort.isBlank()) {
            return defaultField;
        }
        return switch (rawSort.trim().toLowerCase(Locale.ROOT)) {
            case "created_at" -> AdminModerationListSortField.CREATED_AT;
            case "updated_at" -> AdminModerationListSortField.UPDATED_AT;
            case "moderation_status" -> AdminModerationListSortField.MODERATION_STATUS;
            case "like_count" -> AdminModerationListSortField.LIKE_COUNT;
            default -> throw validationError("sort");
        };
    }

    public static AdminCommentListSortField parseCommentSortField(String rawSort) {
        return parseCommentSortField(rawSort, AdminCommentListSortField.CREATED_AT);
    }

    public static AdminCommentListSortField parseCommentSortField(
            String rawSort,
            AdminCommentListSortField defaultField
    ) {
        if (rawSort == null || rawSort.isBlank()) {
            return defaultField;
        }
        return switch (rawSort.trim().toLowerCase(Locale.ROOT)) {
            case "created_at" -> AdminCommentListSortField.CREATED_AT;
            case "updated_at" -> AdminCommentListSortField.UPDATED_AT;
            case "like_count" -> AdminCommentListSortField.LIKE_COUNT;
            default -> throw validationError("sort");
        };
    }

    public static String normalizePostStatus(String rawStatus) {
        return normalizeEnum(rawStatus, POST_STATUSES, "status");
    }

    public static String normalizePostModerationStatus(String rawStatus) {
        return normalizeEnum(rawStatus, POST_MODERATION_STATUSES, "moderation_status");
    }

    public static String normalizeCommentStatus(String rawStatus) {
        return normalizeEnum(rawStatus, COMMENT_STATUSES, "status");
    }

    public static String normalizeQuery(String rawQuery) {
        if (rawQuery == null) {
            return null;
        }
        String trimmed = rawQuery.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizePostId(String rawPostId) {
        if (rawPostId == null) {
            return null;
        }
        String trimmed = rawPostId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static int validatePage(int page) {
        if (page < 1) {
            throw validationError("page");
        }
        return page;
    }

    public static int validateSize(int size, int maxSize) {
        if (size < 1 || size > maxSize) {
            throw validationError("size");
        }
        return size;
    }

    private static String normalizeEnum(String rawValue, Set<String> allowed, String field) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw validationError(field);
        }
        return normalized;
    }

    private static AppException validationError(String field) {
        return new AppException(
                ErrorCode.VALIDATION_ERROR,
                "Du lieu khong hop le.",
                field,
                "INVALID_VALUE"
        );
    }
}
