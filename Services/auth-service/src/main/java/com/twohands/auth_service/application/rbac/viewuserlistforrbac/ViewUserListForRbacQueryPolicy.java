package com.twohands.auth_service.application.rbac.viewuserlistforrbac;

import com.twohands.auth_service.domain.rbac.RbacUserListSortField;
import com.twohands.auth_service.domain.user.UserStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;

import java.util.Locale;
import java.util.Set;

public final class ViewUserListForRbacQueryPolicy {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            UserStatus.PENDING_VERIFICATION.name(),
            UserStatus.ACTIVE.name(),
            UserStatus.SUSPENDED.name()
    );

    private ViewUserListForRbacQueryPolicy() {
    }

    public static RbacUserListSortField parseSortField(String rawSort) {
        return parseSortField(rawSort, RbacUserListSortField.EMAIL);
    }

    public static RbacUserListSortField parseSortField(String rawSort, RbacUserListSortField defaultField) {
        if (rawSort == null || rawSort.isBlank()) {
            return defaultField;
        }

        return switch (rawSort.trim().toLowerCase(Locale.ROOT)) {
            case "email" -> RbacUserListSortField.EMAIL;
            case "display_name" -> RbacUserListSortField.DISPLAY_NAME;
            case "created_at" -> RbacUserListSortField.CREATED_AT;
            case "status" -> RbacUserListSortField.STATUS;
            default -> throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "sort",
                    "INVALID_VALUE"
            );
        };
    }

    public static String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }

        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Du lieu khong hop le.",
                    "status",
                    "INVALID_VALUE"
            );
        }
        return normalized;
    }

    public static String normalizeQuery(String rawQuery) {
        if (rawQuery == null) {
            return null;
        }
        String trimmed = rawQuery.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }
}
