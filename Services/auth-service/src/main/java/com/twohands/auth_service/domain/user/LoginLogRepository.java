package com.twohands.auth_service.domain.user;

import java.util.List;
import java.util.UUID;

public interface LoginLogRepository {
    LoginLog save(LoginLog log);

    List<LoginLog> findByUserId(UUID userId, int limit, int offset);

    LoginLogPage findPageByUserId(UUID userId, LoginLogQueryFilter filter, int limit, int offset);
}
