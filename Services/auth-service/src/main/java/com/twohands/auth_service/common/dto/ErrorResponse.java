package com.twohands.auth_service.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        List<FieldViolation> errors,
        Instant timestamp,
        String traceId
) {
    public record FieldViolation(String field, String message) {
    }
}
