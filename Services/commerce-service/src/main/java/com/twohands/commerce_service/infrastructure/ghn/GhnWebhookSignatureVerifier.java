package com.twohands.commerce_service.infrastructure.ghn;

import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import org.springframework.stereotype.Component;

@Component
public class GhnWebhookSignatureVerifier {

    private final CommerceIntegrationProperties.Ghn ghnProperties;

    public GhnWebhookSignatureVerifier(CommerceIntegrationProperties integrationProperties) {
        this.ghnProperties = integrationProperties.getGhn();
    }

    public boolean verify(String tokenHeader, String authorizationHeader) {
        if (!ghnProperties.isWebhookVerificationEnabled()) {
            return true;
        }
        String secret = ghnProperties.getWebhookSecret().trim();
        if (tokenHeader != null && secret.equals(tokenHeader.trim())) {
            return true;
        }
        if (authorizationHeader != null) {
            String bearer = authorizationHeader.trim();
            if (bearer.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return secret.equals(bearer.substring(7).trim());
            }
            return secret.equals(bearer);
        }
        return false;
    }
}
