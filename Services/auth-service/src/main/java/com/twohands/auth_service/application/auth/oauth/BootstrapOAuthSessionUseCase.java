package com.twohands.auth_service.application.auth.oauth;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import com.twohands.auth_service.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BootstrapOAuthSessionUseCase {

    private final JwtTokenProvider jwtTokenProvider;

    public BootstrapOAuthSessionUseCase(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public BootstrapOAuthSessionResult execute(BootstrapOAuthSessionCommand command) {
        String accessToken = trimToNull(command.accessToken());
        String refreshToken = trimToNull(command.refreshToken());

        if (accessToken == null || refreshToken == null) {
            throw new AppException(ErrorCode.OAUTH_SESSION_INVALID, "Phien OAuth khong hop le hoac da het han.");
        }

        if (!jwtTokenProvider.isValid(accessToken)) {
            throw new AppException(ErrorCode.OAUTH_SESSION_INVALID, "Phien OAuth khong hop le hoac da het han.");
        }

        UUID userId;
        try {
            userId = UUID.fromString(jwtTokenProvider.getSubject(accessToken));
        } catch (IllegalArgumentException ex) {
            throw new AppException(ErrorCode.OAUTH_SESSION_INVALID, "Phien OAuth khong hop le hoac da het han.");
        }

        String email = jwtTokenProvider.getEmail(accessToken);
        String status = jwtTokenProvider.getStatus(accessToken);
        if (email == null || email.isBlank() || status == null || status.isBlank()) {
            throw new AppException(ErrorCode.OAUTH_SESSION_INVALID, "Phien OAuth khong hop le hoac da het han.");
        }

        return new BootstrapOAuthSessionResult(
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpiresInSeconds(accessToken),
                userId,
                email,
                status
        );
    }

    public String successMessage() {
        return "Dang nhap OAuth thanh cong.";
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
