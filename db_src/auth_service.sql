CREATE DATABASE auth_service;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_status AS ENUM (
'PENDING_VERIFICATION',
'ACTIVE',
'INACTIVE',
'SUSPENDED',
'TEMP_BANNED',
'PERMANENT_BANNED',
'UNDER_REVIEW',
'RESTRICTED',
'LOCKED',
'DELETED',
'HARD_DELETED'
);

CREATE TYPE oauth_status AS ENUM (
'LINKED',
'UNLINKED',
'SUSPENDED',
'REVOKED',
'PENDING_VERIFICATION'
);

CREATE TYPE refresh_token_status AS ENUM (
'ACTIVE',
'EXPIRED',
'REVOKED',
'COMPROMISED',
'LOGGED_OUT',
'ROTATED'
);

CREATE TYPE login_failure_reason AS ENUM (
'INVALID_PASSWORD',
'ACCOUNT_LOCKED',
'ACCOUNT_BANNED',
'EMAIL_NOT_VERIFIED',
'OAUTH_REJECTED',
'TOKEN_EXPIRED',
'RATE_LIMITED',
'SUSPICIOUS_ACTIVITY'
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    email TEXT UNIQUE,
    email_normalized TEXT UNIQUE,

    phone TEXT UNIQUE,
    phone_normalized TEXT UNIQUE,

    password_hash TEXT NOT NULL,

    status user_status DEFAULT 'PENDING_VERIFICATION',

    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,

    last_login_at TIMESTAMP,
    password_changed_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code TEXT UNIQUE NOT NULL,
    description TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permission (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,

    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE user_settings (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

    appearance_mode TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,

    display_name TEXT,
    avatar_url TEXT,
    bio TEXT,
    website TEXT,
    social_link TEXT,

    is_private BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    password_hash TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    login_method TEXT,

    ip_address TEXT,
    user_agent TEXT,

    success BOOLEAN DEFAULT TRUE,

    failure_reason login_failure_reason,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE oauth_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    provider TEXT,
    provider_user_id TEXT,

    email TEXT,

    status oauth_status DEFAULT 'LINKED',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_token_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    token_hash TEXT NOT NULL,

    device_id TEXT,
    ip_address TEXT,
    user_agent TEXT,

    expires_at TIMESTAMP,

    revoked BOOLEAN DEFAULT FALSE,

    status refresh_token_status DEFAULT 'ACTIVE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email_norm ON users(email_normalized);
CREATE INDEX idx_users_phone_norm ON users(phone_normalized);

CREATE INDEX idx_login_logs_user ON login_logs(user_id);

CREATE INDEX idx_refresh_token_user ON refresh_token_sessions(user_id);

CREATE INDEX idx_oauth_user ON oauth_accounts(user_id);