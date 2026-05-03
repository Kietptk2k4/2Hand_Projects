package com.twohands.authservice.application.auth.port;

public interface RefreshTokenGenerator {

    /** Returns a cryptographically secure random opaque token (plaintext). */
    String generate();
}
