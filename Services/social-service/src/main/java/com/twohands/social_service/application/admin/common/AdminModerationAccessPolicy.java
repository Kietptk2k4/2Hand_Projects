package com.twohands.social_service.application.admin.common;

import com.twohands.social_service.exception.AppException;
import com.twohands.social_service.exception.ErrorCode;
import com.twohands.social_service.security.AuthenticatedUser;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AdminModerationAccessPolicy {

    private static final Set<String> MODERATION_ROLES = Set.of("ADMIN", "MODERATOR");
    private static final String POST_MODERATE_PERMISSION = "POST_MODERATE";
    private static final String POST_RESTORE_PERMISSION = "POST_RESTORE";
    private static final String COMMENT_MODERATE_PERMISSION = "COMMENT_MODERATE";
    private static final String COMMENT_RESTORE_PERMISSION = "COMMENT_RESTORE";

    private AdminModerationAccessPolicy() {
    }

    public static void ensureCanViewPostList(AuthenticatedUser actor) {
        ensureActor(actor);
        if (!hasPostListAccess(actor)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }

    public static void ensureCanViewCommentList(AuthenticatedUser actor) {
        ensureActor(actor);
        if (!hasCommentListAccess(actor)) {
            throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
    }

    private static boolean hasPostListAccess(AuthenticatedUser actor) {
        return hasModerationRole(actor.roles())
                || hasPermission(actor.permissions(), POST_MODERATE_PERMISSION)
                || hasPermission(actor.permissions(), POST_RESTORE_PERMISSION);
    }

    private static boolean hasCommentListAccess(AuthenticatedUser actor) {
        return hasModerationRole(actor.roles())
                || hasPermission(actor.permissions(), COMMENT_MODERATE_PERMISSION)
                || hasPermission(actor.permissions(), COMMENT_RESTORE_PERMISSION);
    }

    private static boolean hasModerationRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .anyMatch(MODERATION_ROLES::contains);
    }

    private static boolean hasPermission(List<String> permissions, String required) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.stream()
                .map(permission -> permission == null ? "" : permission.trim().toUpperCase(Locale.ROOT))
                .anyMatch(required::equals);
    }

    private static void ensureActor(AuthenticatedUser actor) {
        if (actor == null || actor.userId() == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.defaultMessage());
        }
    }
}
