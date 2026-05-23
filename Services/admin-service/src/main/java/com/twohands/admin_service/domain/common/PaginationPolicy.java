package com.twohands.admin_service.domain.common;

import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

public final class PaginationPolicy {

	public static final int DEFAULT_PAGE = 1;
	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	private PaginationPolicy() {
	}

	public static PageRequest normalize(Integer page, Integer size) {
		int normalizedPage = page == null ? DEFAULT_PAGE : page;
		int normalizedSize = size == null ? DEFAULT_SIZE : size;
		if (normalizedPage < 1) {
			throw paginationError("page", "page must be greater than or equal to 1");
		}
		if (normalizedSize < 1 || normalizedSize > MAX_SIZE) {
			throw paginationError("size", "size must be between 1 and " + MAX_SIZE);
		}
		return new PageRequest(normalizedPage, normalizedSize);
	}

	private static AppException paginationError(String field, String reason) {
		return new AppException(ErrorCode.INVALID_PAGINATION, ErrorCode.INVALID_PAGINATION.defaultMessage(), field, reason);
	}
}
