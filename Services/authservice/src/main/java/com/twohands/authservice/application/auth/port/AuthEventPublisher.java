package com.twohands.authservice.application.auth.port;

public interface AuthEventPublisher {
    void publishVerification(String email, String otp);
}
