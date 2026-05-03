package com.twohands.authservice.application.auth.port;

public interface TokenHasher {

    /** Returns a hex-encoded SHA-256 hash of the given token. Never store plaintext refresh tokens. */
    String hash(String token);
}
