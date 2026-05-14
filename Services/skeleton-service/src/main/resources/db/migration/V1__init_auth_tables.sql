-- =====================================================================
-- AUTH SERVICE DATABASE SCHEMA
-- PostgreSQL
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- ENUMS
-- =====================================================================

CREATE TYPE user_status AS ENUM (
    'PENDING_VERIFICATION',
    'ACTIVE',
    'SUSPENDED',
    'DELETED'
);

CREATE TYPE refresh_token_status AS ENUM (
    'ACTIVE',
    'EXPIRED',
    'REVOKED',
    'LOGGED_OUT'
);

CREATE TYPE verification_token_type AS ENUM (
    'EMAIL_VERIFY',
    'PASSWORD_RESET'
);

CREATE TYPE outbox_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'PUBLISHED',
    'FAILED'
);

-- =====================================================================
-- USERS
-- =====================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255),
    email_normalized VARCHAR(255) UNIQUE,

    phone VARCHAR(30),
    phone_normalized VARCHAR(30) UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    status user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',

    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,

    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,

    deleted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- ROLES
-- =====================================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- PERMISSIONS
-- =====================================================================

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- ROLE PERMISSIONS
-- =====================================================================

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- USER ROLES
-- =====================================================================

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- USER SETTINGS
-- =====================================================================

CREATE TABLE user_settings (
    user_id UUID PRIMARY KEY,

    appearance_mode VARCHAR(30) DEFAULT 'LIGHT',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_settings_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- USER PROFILES
-- =====================================================================

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY,

    display_name VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    bio TEXT,
    website TEXT,

    social_links JSONB,

    is_private BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- LOGIN LOGS
-- =====================================================================

CREATE TABLE login_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID,

    login_method VARCHAR(50),
    ip_address VARCHAR(100),
    user_agent TEXT,

    success BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_login_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

-- =====================================================================
-- OAUTH ACCOUNTS
-- =====================================================================

CREATE TABLE oauth_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,

    email VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_oauth_accounts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_oauth_provider_user
        UNIQUE (provider, provider_user_id)
);

-- =====================================================================
-- REFRESH TOKEN SESSIONS
-- =====================================================================

CREATE TABLE refresh_token_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    token_hash TEXT NOT NULL UNIQUE,

    device_id VARCHAR(255),

    ip_address VARCHAR(100),
    user_agent TEXT,

    expires_at TIMESTAMP NOT NULL,

    status refresh_token_status NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- OUTBOX EVENTS
-- =====================================================================

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    event_type VARCHAR(255) NOT NULL,
    source VARCHAR(100) NOT NULL,

    payload JSONB NOT NULL,

    status outbox_status NOT NULL DEFAULT 'PENDING',

    retry_count INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP,

    last_error TEXT
);

-- =====================================================================
-- VERIFICATION TOKENS
-- =====================================================================

CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    token_hash TEXT NOT NULL,

    type verification_token_type NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    used_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_verification_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- INDEXES
-- =====================================================================

CREATE INDEX idx_users_email_norm
ON users(email_normalized);

CREATE INDEX idx_users_phone_norm
ON users(phone_normalized);

CREATE INDEX idx_login_logs_user_created
ON login_logs(user_id, created_at DESC);

CREATE INDEX idx_refresh_token_user
ON refresh_token_sessions(user_id);

CREATE INDEX idx_oauth_user
ON oauth_accounts(user_id);

CREATE INDEX idx_outbox_status
ON outbox_events(status);

CREATE INDEX idx_verification_token_user
ON verification_tokens(user_id);

CREATE INDEX idx_verification_token_hash
ON verification_tokens(token_hash);

-- =====================================================================
-- DEFAULT ROLES
-- =====================================================================

INSERT INTO roles(code, name)
VALUES
('USER', 'Normal User'),
('ADMIN', 'Administrator'),
('MODERATOR', 'Moderator');

-- =====================================================================
-- DEFAULT PERMISSIONS
-- =====================================================================

INSERT INTO permissions(code, description)
VALUES
('USER_READ', 'Read user information'),
('USER_UPDATE', 'Update user information'),
('USER_DELETE', 'Delete user account'),
('ADMIN_ACCESS', 'Access admin dashboard');
