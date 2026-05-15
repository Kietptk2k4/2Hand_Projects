package com.twohands.auth_service.infrastructure.security;

import com.twohands.auth_service.application.auth.register.PasswordHashingService;
import com.twohands.auth_service.domain.user.PasswordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHashingService implements PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHashingService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        return PasswordHash.of(passwordEncoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword.value());
    }
}
