-- =====================================================================
-- ADMIN SERVICE DATABASE SCHEMA (MVP)
-- PostgreSQL
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- ENUMS
-- =====================================================================

CREATE TYPE system_config_value_type AS ENUM (
    'INTEGER',
    'DECIMAL',
    'STRING',
    'BOOLEAN',
    'JSON'
);

CREATE TYPE announcement_severity AS ENUM (
    'INFO',
    'WARNING',
    'CRITICAL'
);

CREATE TYPE announcement_status AS ENUM (
    'DRAFT',
    'SENT',
    'CANCELLED'
);

CREATE TYPE admin_action_type AS ENUM (
    'USER_SUSPEND',
    'PRODUCT_REMOVE',
    'REVIEW_HIDE',
    'REFUND_EXECUTE',
    'USER_BAN',
    'USER_RESTRICT',
    'USER_ENFORCEMENT_REVOKE',
    'SHOP_SUSPEND',
    'SHOP_CLOSE',
    'POST_MODERATE',
    'COMMENT_MODERATE',
    'SYSTEM_CONFIG_CREATE',
    'SYSTEM_CONFIG_UPDATE',
    'SYSTEM_CONFIG_TOGGLE',
    'SYSTEM_ANNOUNCEMENT_PUBLISH',
    'ORDER_SUPPORT_VIEW',
    'PAYMENT_SUPPORT_VIEW',
    'SHIPMENT_SUPPORT_VIEW',
    'ADMIN_ACCESS_DENIED',
    'ADMIN_SESSION_REVOKE',
    'SYSTEM_ANNOUNCEMENT_CREATE',
    'SYSTEM_ANNOUNCEMENT_PIN',
    'SYSTEM_ANNOUNCEMENT_CANCEL',
    'PRODUCT_RESTORE',
    'REVIEW_REMOVE',
    'REVIEW_RESTORE'
);

CREATE TYPE user_enforcement_action_type AS ENUM (
    'BAN',
    'SUSPEND',
    'RESTRICT'
);

CREATE TYPE user_enforcement_status AS ENUM (
    'ACTIVE',
    'REVOKED',
    'EXPIRED'
);

CREATE TYPE content_moderation_target_type AS ENUM (
    'POST',
    'COMMENT',
    'PRODUCT',
    'REVIEW'
);

CREATE TYPE content_moderation_action AS ENUM (
    'HIDE',
    'REMOVE',
    'RESTORE'
);

CREATE TYPE outbox_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'PUBLISHED',
    'FAILED'
);

-- =====================================================================
-- TABLES
-- =====================================================================

CREATE TABLE system_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT NOT NULL,
    value_type system_config_value_type NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE system_config_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(255) NOT NULL,
    old_value TEXT,
    new_value TEXT NOT NULL,
    changed_by UUID NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE system_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    severity announcement_severity NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    dismissible BOOLEAN NOT NULL DEFAULT TRUE,
    status announcement_status NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP
);

CREATE TABLE admin_action_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL,
    action_type admin_action_type NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    request_payload JSONB,
    response_payload JSONB,
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_enforcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    action_type user_enforcement_action_type NOT NULL,
    reason_code VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    expires_at TIMESTAMP,
    enforced_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status user_enforcement_status NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_enforcement_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enforcement_id UUID NOT NULL REFERENCES user_enforcements(id),
    old_status user_enforcement_status,
    new_status user_enforcement_status NOT NULL,
    admin_id UUID,
    note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE content_moderation_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type content_moderation_target_type NOT NULL,
    target_id VARCHAR(255),
    action content_moderation_action NOT NULL,
    reason TEXT NOT NULL,
    admin_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    note TEXT
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(255) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload JSONB NOT NULL,
    status outbox_status NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP
);

-- =====================================================================
-- INDEXES
-- =====================================================================

CREATE UNIQUE INDEX idx_system_configs_key ON system_configs(config_key);

CREATE INDEX idx_config_history_key_created
ON system_config_history(config_key, created_at DESC);

CREATE INDEX idx_announcements_status_created
ON system_announcements(status, created_at DESC);

CREATE INDEX idx_admin_logs_admin_created
ON admin_action_logs(admin_id, created_at DESC);

CREATE INDEX idx_admin_logs_target
ON admin_action_logs(target_type, target_id, created_at DESC);

CREATE INDEX idx_enforcement_user ON user_enforcements(user_id);

CREATE INDEX idx_enforcement_user_status ON user_enforcements(user_id, status);

CREATE INDEX idx_enforcement_active_expiring
ON user_enforcements(status, expires_at)
WHERE status = 'ACTIVE' AND expires_at IS NOT NULL;

CREATE INDEX idx_enforcement_logs_enforcement
ON user_enforcement_logs(enforcement_id, created_at DESC);

CREATE INDEX idx_moderation_target
ON content_moderation_logs(target_type, target_id);

CREATE INDEX idx_outbox_pending
ON outbox_events(status, created_at)
WHERE status = 'PENDING';
