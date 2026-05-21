CREATE TABLE IF NOT EXISTS system_configs (
    id UUID PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value VARCHAR(4000) NOT NULL,
    value_type VARCHAR(50) NOT NULL,
    description VARCHAR(4000),
    is_active BOOLEAN NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_by UUID,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS system_config_history (
    id UUID PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL,
    old_value VARCHAR(4000),
    new_value VARCHAR(4000) NOT NULL,
    changed_by UUID NOT NULL,
    reason VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS system_announcements (
    id UUID PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content VARCHAR(4000) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    is_pinned BOOLEAN NOT NULL,
    dismissible BOOLEAN NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_action_logs (
    id UUID PRIMARY KEY,
    admin_id UUID NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    request_payload VARCHAR(4000),
    response_payload VARCHAR(4000),
    ip_address VARCHAR(100),
    user_agent VARCHAR(4000),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_enforcements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    reason_code VARCHAR(100) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    expires_at TIMESTAMP,
    enforced_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS user_enforcement_logs (
    id UUID PRIMARY KEY,
    enforcement_id UUID NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    admin_id UUID,
    note VARCHAR(4000),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS content_moderation_logs (
    id UUID PRIMARY KEY,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    reason VARCHAR(4000) NOT NULL,
    admin_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    note VARCHAR(4000)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload VARCHAR(4000) NOT NULL,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);
