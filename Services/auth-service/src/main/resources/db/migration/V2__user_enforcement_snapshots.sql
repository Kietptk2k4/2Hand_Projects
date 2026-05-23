CREATE TABLE user_enforcement_snapshots (
    enforcement_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason_code VARCHAR(100),
    description TEXT,
    expires_at TIMESTAMP,
    event_id UUID,
    applied_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_enforcement_snapshots_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX uq_user_enforcement_snapshots_event_id
    ON user_enforcement_snapshots(event_id)
    WHERE event_id IS NOT NULL;

CREATE INDEX idx_user_enforcement_snapshots_user_status
    ON user_enforcement_snapshots(user_id, status);
