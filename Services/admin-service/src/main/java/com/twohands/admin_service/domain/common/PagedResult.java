package com.twohands.admin_service.domain.common;

import java.util.List;

public record PagedResult<T>(
		List<T> items,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
