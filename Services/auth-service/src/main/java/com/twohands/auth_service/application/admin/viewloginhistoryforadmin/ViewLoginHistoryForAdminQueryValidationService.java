package com.twohands.auth_service.application.admin.viewloginhistoryforadmin;

import com.twohands.auth_service.domain.user.LoginLogQueryFilter;
import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Service
public class ViewLoginHistoryForAdminQueryValidationService {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

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

    public LoginLogQueryFilter validateFilters(Boolean success, String from, String to) {
        Instant fromInstant = parseInstant(from, "from");
        Instant toInstant = parseInstant(to, "to");

        if (fromInstant != null && toInstant != null && fromInstant.isAfter(toInstant)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "From must be before or equal to to",
                    "from",
                    "INVALID_RANGE"
            );
        }

        return new LoginLogQueryFilter(success, fromInstant, toInstant);
    }

    private Instant parseInstant(String rawValue, String field) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(rawValue.trim());
        } catch (DateTimeParseException ex) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    field + " must be a valid ISO-8601 instant",
                    field,
                    "INVALID_FORMAT"
            );
        }
    }
}
