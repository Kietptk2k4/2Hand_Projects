package com.twohands.auth_service.application.auth.oauth;

import com.twohands.auth_service.exception.AppException;
import com.twohands.auth_service.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class ExchangeOAuthCodeUseCase {

    private final OAuthExchangeCodeStore exchangeCodeStore;

    public ExchangeOAuthCodeUseCase(OAuthExchangeCodeStore exchangeCodeStore) {
        this.exchangeCodeStore = exchangeCodeStore;
    }

    public OAuthExchangeCodePayload execute(String code) {
        return exchangeCodeStore.consume(code)
                .orElseThrow(() -> new AppException(
                        ErrorCode.OAUTH_SESSION_INVALID,
                        "Phien OAuth khong hop le hoac da het han."
                ));
    }

    public String successMessage() {
        return "Dang nhap OAuth thanh cong.";
    }
}
