package com.twohands.auth_service.application.admin.viewusersessionsforadmin;

import com.twohands.auth_service.domain.session.SessionStatus;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ViewUserSessionsForAdminQueryValidationService {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 50;

    public int validatePage(int page) {
        if (page < 1) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Page must be greater than or equal to 1",
                    "page",
                    "INVALID_VALUE"
            );
        }
        return page;
    }

    public int validateLimit(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Limit must be between 1 and " + MAX_LIMIT,
                    "limit",
                    "INVALID_VALUE"
            );
        }
        return limit;
    }

    public SessionStatusFilter validateStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return SessionStatusFilter.activeOnly();
        }

        String normalized = rawStatus.trim().toUpperCase();
        if ("ALL".equals(normalized)) {
            return SessionStatusFilter.all();
        }

        try {
            return SessionStatusFilter.of(SessionStatus.valueOf(normalized));
        } catch (IllegalArgumentException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Status is not supported",
                    "status",
                    "INVALID_VALUE"
            );
        }
    }

    public record SessionStatusFilter(SessionStatus status) {
        public static SessionStatusFilter activeOnly() {
            return new SessionStatusFilter(SessionStatus.ACTIVE);
        }

        public static SessionStatusFilter all() {
            return new SessionStatusFilter(null);
        }

        public static SessionStatusFilter of(SessionStatus status) {
            return new SessionStatusFilter(status);
        }
    }
}
