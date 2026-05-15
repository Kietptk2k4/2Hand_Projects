package com.twohands.auth_service.application.auth.register;

import com.twohands.auth_service.domain.user.PasswordHash;

public interface PasswordHashingService {
    PasswordHash hash(String rawPassword);
}
