package com.twohands.admin_service.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	INTERNAL_ERROR("ADMIN-500", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
	BAD_REQUEST("ADMIN-400", HttpStatus.BAD_REQUEST, "Invalid request"),
	VALIDATION_ERROR("ADMIN-400-VALIDATION", HttpStatus.BAD_REQUEST, "Validation failed"),
	UNAUTHORIZED("ADMIN-401", HttpStatus.UNAUTHORIZED, "Authentication required"),
	FORBIDDEN("ADMIN-403", HttpStatus.FORBIDDEN, "Access denied"),
	RESOURCE_NOT_FOUND("ADMIN-404", HttpStatus.NOT_FOUND, "Resource not found"),
	INVALID_PAGINATION("ADMIN-400-PAGINATION", HttpStatus.BAD_REQUEST, "Invalid pagination parameters");

	private final String code;
	private final HttpStatus status;
	private final String defaultMessage;

	ErrorCode(String code, HttpStatus status, String defaultMessage) {
		this.code = code;
		this.status = status;
		this.defaultMessage = defaultMessage;
	}

	public String code() {
		return code;
	}

	public HttpStatus status() {
		return status;
	}

	public String defaultMessage() {
		return defaultMessage;
	}
}
