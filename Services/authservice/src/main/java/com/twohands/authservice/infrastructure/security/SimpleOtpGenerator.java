package com.twohands.authservice.infrastructure.security;

import com.twohands.authservice.application.auth.port.OtpGenerator;
import org.springframework.stereotype.Component;

@Component
public class SimpleOtpGenerator implements OtpGenerator {

    @Override
    public String generate() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
