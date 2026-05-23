package com.twohands.auth_service.domain.user;

import java.util.List;

public record LoginLogPage(
        List<LoginLog> items,
        long totalItems
) {
}
