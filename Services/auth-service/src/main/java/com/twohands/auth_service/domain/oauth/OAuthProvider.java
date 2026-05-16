package com.twohands.auth_service.domain.oauth;

import com.twohands.auth_service.domain.user.LoginMethod;

public enum OAuthProvider {
    GOOGLE(LoginMethod.GOOGLE),
    FACEBOOK(LoginMethod.FACEBOOK);

    private final LoginMethod loginMethod;

    OAuthProvider(LoginMethod loginMethod) {
        this.loginMethod = loginMethod;
    }

    public LoginMethod loginMethod() {
        return loginMethod;
    }
}
