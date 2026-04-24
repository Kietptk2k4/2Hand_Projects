package com.twohands.authservice.domain.user;

import com.twohands.authservice.domain.login.LoginLog;
import com.twohands.authservice.domain.oauth.OAuthAccount;
import com.twohands.authservice.domain.role.Role;
import com.twohands.authservice.domain.session.RefreshTokenSession;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class User {

    private UUID id;
    private String email;
    private String emailNormalized;
    private String phone;
    private String passwordHash;
    private UserStatus status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Instant passwordChangedAt;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Set<Role> roles = new HashSet<>();
    private UserSettings settings;
    private UserProfile profile;
    private List<LoginLog> loginLogs = new ArrayList<>();
    private List<OAuthAccount> oauthAccounts = new ArrayList<>();
    private List<RefreshTokenSession> refreshTokenSessions = new ArrayList<>();

    public User() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailNormalized() {
        return emailNormalized;
    }

    public void setEmailNormalized(String emailNormalized) {
        this.emailNormalized = emailNormalized;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public List<LoginLog> getLoginLogs() {
        return loginLogs;
    }

    public void setLoginLogs(List<LoginLog> loginLogs) {
        this.loginLogs = loginLogs;
    }

    public List<OAuthAccount> getOauthAccounts() {
        return oauthAccounts;
    }

    public void setOauthAccounts(List<OAuthAccount> oauthAccounts) {
        this.oauthAccounts = oauthAccounts;
    }

    public List<RefreshTokenSession> getRefreshTokenSessions() {
        return refreshTokenSessions;
    }

    public void setRefreshTokenSessions(List<RefreshTokenSession> refreshTokenSessions) {
        this.refreshTokenSessions = refreshTokenSessions;
    }
}
