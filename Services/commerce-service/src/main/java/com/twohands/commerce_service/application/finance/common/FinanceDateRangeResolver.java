package com.twohands.commerce_service.application.finance.common;

import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public final class FinanceDateRangeResolver {

    private static final int MAX_TREND_RANGE_DAYS = 366;

    private FinanceDateRangeResolver() {
    }

    public static Optional<Instant> parseOptionalInstant(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(value.trim()));
        } catch (DateTimeParseException ex) {
            try {
                LocalDate date = LocalDate.parse(value.trim());
                if ("to".equals(fieldName)) {
                    return Optional.of(date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
                }
                return Optional.of(date.atStartOfDay(ZoneOffset.UTC).toInstant());
            } catch (DateTimeParseException nested) {
                throw new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        fieldName + " must be ISO-8601 instant or date",
                        fieldName,
                        "invalid format"
                );
            }
        }
    }

    public static void validateSummaryRange(Optional<Instant> from, Optional<Instant> toExclusive) {
        if (from.isPresent() != toExclusive.isPresent()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "from and to must be provided together",
                    "from",
                    "must be provided with to"
            );
        }
        if (from.isPresent() && !from.get().isBefore(toExclusive.get())) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "from must be before to",
                    "from",
                    "must be before to"
            );
        }
    }

    public static void validateTrendRange(Instant from, Instant toExclusive) {
        if (!from.isBefore(toExclusive)) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "from must be before to",
                    "from",
                    "must be before to"
            );
        }
        long days = ChronoUnit.DAYS.between(from, toExclusive);
        if (days > MAX_TREND_RANGE_DAYS) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "date range must not exceed " + MAX_TREND_RANGE_DAYS + " days",
                    "to",
                    "range too large"
            );
        }
    }
}
