-- =====================================================================
-- NOTIFICATION SERVICE DATABASE SCHEMA (MVP)
-- PostgreSQL
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =====================================================================
-- ENUMS
-- =====================================================================

CREATE TYPE notification_event_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE notification_delivery_status AS ENUM ('PENDING', 'SENT', 'FAILED');
CREATE TYPE notification_source_service AS ENUM ('AUTH', 'SOCIAL', 'COMMERCE', 'ADMIN', 'SYSTEM');
CREATE TYPE device_type AS ENUM ('IOS', 'ANDROID', 'WEB');

-- =====================================================================
-- TABLES
-- =====================================================================

CREATE TABLE notification_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_event_id UUID NULL,
    event_key VARCHAR(255) NULL,
    event_type VARCHAR(100) NOT NULL,
    source_service notification_source_service NOT NULL,
    aggregate_type VARCHAR(80) NULL,
    aggregate_id VARCHAR(100) NULL,
    actor_id UUID NULL,
    recipient_user_id UUID NULL,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    status notification_event_status NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 5,
    last_error TEXT NULL,
    locked_at TIMESTAMPTZ NULL,
    locked_by VARCHAR(100) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_notification_events_retry_count
        CHECK (retry_count >= 0 AND max_retry_count >= 0 AND retry_count <= max_retry_count),
    CONSTRAINT chk_notification_events_processed_at
        CHECK ((status = 'COMPLETED' AND processed_at IS NOT NULL) OR (status <> 'COMPLETED'))
);

CREATE UNIQUE INDEX uq_notification_events_source_event
    ON notification_events(source_service, source_event_id)
    WHERE source_event_id IS NOT NULL;

CREATE UNIQUE INDEX uq_notification_events_event_key
    ON notification_events(source_service, event_key)
    WHERE event_key IS NOT NULL;

CREATE INDEX idx_notification_events_status
    ON notification_events(status, created_at);

CREATE INDEX idx_notification_events_retry
    ON notification_events(status, retry_count, created_at)
    WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX idx_notification_events_locked
    ON notification_events(status, locked_at)
    WHERE status = 'PROCESSING';

CREATE INDEX idx_notification_events_source_type
    ON notification_events(source_service, event_type, created_at);

CREATE INDEX idx_notification_events_recipient
    ON notification_events(recipient_user_id, created_at)
    WHERE recipient_user_id IS NOT NULL;

CREATE TABLE user_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_event_id UUID NULL REFERENCES notification_events(id) ON DELETE SET NULL,
    user_id UUID NOT NULL,
    actor_id UUID NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    reference_type VARCHAR(80) NULL,
    reference_id VARCHAR(100) NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    delivery_status notification_delivery_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_user_notifications_read_at
        CHECK ((is_read = false AND read_at IS NULL) OR (is_read = true))
);

CREATE UNIQUE INDEX uq_user_notifications_event_recipient_reference
    ON user_notifications(
        notification_event_id,
        user_id,
        type,
        COALESCE(reference_type, ''),
        COALESCE(reference_id, '')
    )
    WHERE notification_event_id IS NOT NULL;

CREATE INDEX idx_user_notifications_user_created
    ON user_notifications(user_id, created_at DESC)
    WHERE is_deleted = false;

CREATE INDEX idx_user_notifications_user_unread
    ON user_notifications(user_id, is_read, created_at DESC)
    WHERE is_deleted = false;

CREATE INDEX idx_user_notifications_reference
    ON user_notifications(reference_type, reference_id)
    WHERE reference_type IS NOT NULL AND reference_id IS NOT NULL;

CREATE INDEX idx_user_notifications_event
    ON user_notifications(notification_event_id)
    WHERE notification_event_id IS NOT NULL;

CREATE INDEX idx_user_notifications_delivery_status
    ON user_notifications(delivery_status, created_at)
    WHERE delivery_status IN ('PENDING', 'FAILED');

CREATE TABLE user_device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_type device_type NOT NULL,
    device_token VARCHAR(512) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_device_tokens_device_token UNIQUE (device_token)
);

CREATE UNIQUE INDEX uq_user_device_tokens_user_token
    ON user_device_tokens(user_id, device_token);

CREATE INDEX idx_user_device_tokens_user_active
    ON user_device_tokens(user_id, is_active, updated_at DESC);

CREATE INDEX idx_user_device_tokens_active_type
    ON user_device_tokens(device_type, is_active);

CREATE INDEX idx_user_device_tokens_last_used
    ON user_device_tokens(last_used_at)
    WHERE is_active = true;

CREATE TABLE user_notification_settings (
    user_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    allow_push BOOLEAN NOT NULL DEFAULT true,
    allow_email BOOLEAN NOT NULL DEFAULT false,
    allow_in_app BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, event_type)
);

CREATE INDEX idx_user_notification_settings_event_type
    ON user_notification_settings(event_type);

CREATE INDEX idx_user_notification_settings_user_updated
    ON user_notification_settings(user_id, updated_at DESC);
