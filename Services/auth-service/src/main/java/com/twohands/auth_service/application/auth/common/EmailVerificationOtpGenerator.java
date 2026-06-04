package com.twohands.auth_service.application.auth.common;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class EmailVerificationOtpGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return String.valueOf(100_000 + secureRandom.nextInt(900_000));
    }
}
