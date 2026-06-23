package com.twohands.auth_service.unit.infrastructure.security.oauth;

import com.twohands.auth_service.infrastructure.security.oauth.OAuthRedirectUriValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthRedirectUriValidatorTest {

    private OAuthRedirectUriValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OAuthRedirectUriValidator(
                "http://localhost:5173,http://127.0.0.1:5173",
                "twohands"
        );
    }

    @Test
    void shouldAllowWebSuccessCallback() {
        assertTrue(validator.isAllowed("http://localhost:5173/oauth/success", new MockHttpServletRequest()));
    }

    @Test
    void shouldAllowAppDeepLinkCallback() {
        assertTrue(validator.isAllowed("twohands://oauth/success", new MockHttpServletRequest()));
    }

    @Test
    void shouldAllowSameOriginMobileBridge() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("192.168.1.24");
        request.setServerPort(3001);

        String bridge = "http://192.168.1.24:3001/api/v1/auth/oauth/mobile-complete?app_return=twohands%3A%2F%2Foauth%2Fsuccess";
        assertTrue(validator.isAllowed(bridge, request));
    }

    @Test
    void shouldRejectMobileBridgeOnDifferentHost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("192.168.1.24");
        request.setServerPort(3001);

        String bridge = "http://10.0.0.9:3001/api/v1/auth/oauth/mobile-complete?app_return=twohands%3A%2F%2Foauth%2Fsuccess";
        assertFalse(validator.isAllowed(bridge, request));
    }

    @Test
    void shouldRejectUnknownWebOrigin() {
        assertFalse(validator.isAllowed("http://evil.example/oauth/success", new MockHttpServletRequest()));
    }
}
