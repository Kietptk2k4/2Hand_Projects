package com.twohands.admin_service.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
		int code,
		boolean success,
		String message,
		T data,
		List<ApiError> errors,
		Instant timestamp
) {
	public static <T> ApiResponse<T> success(int code, String message, T data) {
		return new ApiResponse<>(code, true, message, data, null, Instant.now());
	}

	public static ApiResponse<Void> error(int code, String message, List<ApiError> errors) {
		return new ApiResponse<>(code, false, message, null, errors, Instant.now());
	}

	public record ApiError(String field, String reason) {
	}
}
