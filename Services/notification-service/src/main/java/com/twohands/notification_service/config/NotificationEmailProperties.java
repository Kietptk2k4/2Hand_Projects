package com.twohands.notification_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.integrations.email")
public class NotificationEmailProperties {

    private boolean enabled;
    private String fromAddress = "noreply@2hands.vn";
    private String fromName = "2Hands";
    private String verificationLinkBaseUrl;
    private String passwordResetLinkBaseUrl;

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String fromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String fromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String verificationLinkBaseUrl() {
        return verificationLinkBaseUrl;
    }

    public void setVerificationLinkBaseUrl(String verificationLinkBaseUrl) {
        this.verificationLinkBaseUrl = verificationLinkBaseUrl;
    }

    public String passwordResetLinkBaseUrl() {
        return passwordResetLinkBaseUrl;
    }

    public void setPasswordResetLinkBaseUrl(String passwordResetLinkBaseUrl) {
        this.passwordResetLinkBaseUrl = passwordResetLinkBaseUrl;
    }
}
