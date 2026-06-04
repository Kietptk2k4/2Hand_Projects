CREATE TABLE IF NOT EXISTS notification_events (
    id UUID PRIMARY KEY,
    source_event_id UUID,
    event_key VARCHAR(255),
    event_type VARCHAR(100) NOT NULL,
    source_service VARCHAR(50) NOT NULL,
    aggregate_type VARCHAR(80),
    aggregate_id VARCHAR(100),
    actor_id UUID,
    recipient_user_id UUID,
    payload JSON NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry_count INTEGER NOT NULL DEFAULT 5,
    last_error VARCHAR(2000),
    locked_at TIMESTAMP,
    locked_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_events_source_event
    ON notification_events(source_service, source_event_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_events_event_key
    ON notification_events(source_service, event_key);

CREATE TABLE IF NOT EXISTS user_notifications (
    id UUID PRIMARY KEY,
    notification_event_id UUID,
    user_id UUID NOT NULL,
    actor_id UUID,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    reference_type VARCHAR(80) NOT NULL DEFAULT '',
    reference_id VARCHAR(100) NOT NULL DEFAULT '',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSON NOT NULL,
    delivery_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_notifications_event_recipient_reference
    ON user_notifications(notification_event_id, user_id, type, reference_type, reference_id);

CREATE TABLE IF NOT EXISTS user_device_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    device_token VARCHAR(512) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_notification_settings (
    user_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    allow_push BOOLEAN NOT NULL DEFAULT TRUE,
    allow_email BOOLEAN NOT NULL DEFAULT FALSE,
    allow_in_app BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, event_type)
);
